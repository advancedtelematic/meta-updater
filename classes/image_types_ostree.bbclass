# OSTree deployment
inherit distro_features_check

OSTREE_KERNEL ??= "${KERNEL_IMAGETYPE}"
OSTREE_ROOTFS ??= "${WORKDIR}/ostree-rootfs"
OSTREE_COMMIT_SUBJECT ??= "Commit-id: ${IMAGE_NAME}"
OSTREE_COMMIT_BODY ??= ""
OSTREE_UPDATE_SUMMARY ??= "0"

BUILD_OSTREE_TARBALL ??= "1"

SYSTEMD_USED = "${@oe.utils.ifelse(d.getVar('VIRTUAL-RUNTIME_init_manager', True) == 'systemd', 'true', '')}"

IMAGE_CMD_TAR = "tar --xattrs --xattrs-include=*"
CONVERSION_CMD_tar = "touch ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.${type}; ${IMAGE_CMD_TAR} --numeric-owner -cf ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.${type}.tar -C ${OTA_IMAGE_ROOTFS} . || [ $? -eq 1 ]"
CONVERSIONTYPES_append = " tar"

REQUIRED_DISTRO_FEATURES = "usrmerge"
OTA_IMAGE_ROOTFS_task-image-ostree = "${OSTREE_ROOTFS}"
do_image_ostree[dirs] = "${OSTREE_ROOTFS}"
do_image_ostree[cleandirs] = "${OSTREE_ROOTFS}"
do_image_ostree[depends] = "coreutils-native:do_populate_sysroot virtual/kernel:do_deploy ${INITRAMFS_IMAGE}:do_image_complete"
IMAGE_CMD_ostree () {
    cp -a ${IMAGE_ROOTFS}/* ${OSTREE_ROOTFS}
    chmod a+rx ${OSTREE_ROOTFS}
    sync

    for d in var/*; do
      if [ "${d}" != "var/local" ]; then
        rm -rf ${d}
      fi
    done

    # Create sysroot directory to which physical sysroot will be mounted
    mkdir sysroot
    ln -sf sysroot/ostree ostree

    rm -rf tmp/*
    ln -sf sysroot/tmp tmp

    mkdir -p usr/rootdirs

    mv etc usr/
    # Implement UsrMove
    dirs="bin sbin lib"

    for dir in ${dirs} ; do
        if [ -d ${dir} ] && [ ! -L ${dir} ] ; then
            mv ${dir} usr/rootdirs/
            rm -rf ${dir}
            ln -sf usr/rootdirs/${dir} ${dir}
        fi
    done

    if [ -n "${SYSTEMD_USED}" ]; then
        mkdir -p usr/etc/tmpfiles.d
        tmpfiles_conf=usr/etc/tmpfiles.d/00ostree-tmpfiles.conf
        echo "d /var/rootdirs 0755 root root -" >>${tmpfiles_conf}
        echo "L /var/rootdirs/home - - - - /sysroot/home" >>${tmpfiles_conf}
    else
        mkdir -p usr/etc/init.d
        tmpfiles_conf=usr/etc/init.d/tmpfiles.sh
        echo '#!/bin/sh' > ${tmpfiles_conf}
        echo "mkdir -p /var/rootdirs; chmod 755 /var/rootdirs" >> ${tmpfiles_conf}
        echo "ln -sf /sysroot/home /var/rootdirs/home" >> ${tmpfiles_conf}

        ln -s ../init.d/tmpfiles.sh usr/etc/rcS.d/S20tmpfiles.sh
    fi

    # Preserve OSTREE_BRANCHNAME for future information
    mkdir -p usr/share/sota/
    echo -n "${OSTREE_BRANCHNAME}" > usr/share/sota/branchname

    # Preserve data in /home to be later copied to /sysroot/home by sysroot
    # generating procedure
    mkdir -p usr/homedirs
    if [ -d "home" ] && [ ! -L "home" ]; then
        mv home usr/homedirs/home
        ln -sf var/rootdirs/home home
    fi

    # Move persistent directories to /var
    dirs="opt mnt media srv"

    for dir in ${dirs}; do
        if [ -d ${dir} ] && [ ! -L ${dir} ]; then
            if [ "$(ls -A $dir)" ]; then
                bbwarn "Data in /$dir directory is not preserved by OSTree. Consider moving it under /usr"
            fi

            if [ -n "${SYSTEMD_USED}" ]; then
                echo "d /var/rootdirs/${dir} 0755 root root -" >>${tmpfiles_conf}
            else
                echo "mkdir -p /var/rootdirs/${dir}; chown 755 /var/rootdirs/${dir}" >>${tmpfiles_conf}
            fi
            rm -rf ${dir}
            ln -sf var/rootdirs/${dir} ${dir}
        fi
    done

    if [ -d root ] && [ ! -L root ]; then
        if [ "$(ls -A root)" ]; then
            bbfatal "Data in /root directory is not preserved by OSTree."
        fi

        if [ -n "${SYSTEMD_USED}" ]; then
            echo "d /var/roothome 0755 root root -" >>${tmpfiles_conf}
        else
            echo "mkdir -p /var/roothome; chown 755 /var/roothome" >>${tmpfiles_conf}
        fi

        rm -rf root
        ln -sf var/roothome root
    fi

    checksum=`sha256sum ${DEPLOY_DIR_IMAGE}/${OSTREE_KERNEL} | cut -f 1 -d " "`

    cp ${DEPLOY_DIR_IMAGE}/${OSTREE_KERNEL} boot/vmlinuz-${checksum}

    if [ "${KERNEL_IMAGETYPE}" = "fitImage" ]; then
        # this is a hack for ostree not to override init= in kernel cmdline -
        # make it think that the initramfs is present (while it is in FIT image)
        touch boot/initramfs-${checksum}
    else
        cp ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES} boot/initramfs-${checksum}
    fi

    # Copy image manifest
    cat ${IMAGE_MANIFEST} | cut -d " " -f1,3 > usr/package.manifest
}

IMAGE_TYPEDEP_ostreecommit = "ostree"
do_image_ostreecommit[depends] += "ostree-native:do_populate_sysroot"
do_image_ostreecommit[lockfiles] += "${OSTREE_REPO}/ostree.lock"
IMAGE_CMD_ostreecommit () {
    if ! ostree --repo=${OSTREE_REPO} refs 2>&1 > /dev/null; then
        ostree --repo=${OSTREE_REPO} init --mode=archive-z2
    fi

    # Commit the result
    ostree --repo=${OSTREE_REPO} commit \
           --tree=dir=${OSTREE_ROOTFS} \
           --skip-if-unchanged \
           --branch=${OSTREE_BRANCHNAME} \
           --subject="${OSTREE_COMMIT_SUBJECT}" \
           --body="${OSTREE_COMMIT_BODY}"

    if [ "${OSTREE_UPDATE_SUMMARY}" = "1" ]; then
        ostree --repo=${OSTREE_REPO} summary -u
    fi
}

IMAGE_TYPEDEP_ostreepush = "ostreecommit"
do_image_ostreepush[depends] += "aktualizr-native:do_populate_sysroot ca-certificates-native:do_populate_sysroot"
IMAGE_CMD_ostreepush () {
    # Print warnings if credetials are not set or if the file has not been found.
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        if [ -e ${SOTA_PACKED_CREDENTIALS} ]; then
            garage-push -vv --repo=${OSTREE_REPO} \
                        --ref=${OSTREE_BRANCHNAME} \
                        --credentials=${SOTA_PACKED_CREDENTIALS} \
                        --cacert=${STAGING_ETCDIR_NATIVE}/ssl/certs/ca-certificates.crt
        else
            bbwarn "SOTA_PACKED_CREDENTIALS file does not exist."
        fi
    else
        bbwarn "SOTA_PACKED_CREDENTIALS not set. Please add SOTA_PACKED_CREDENTIALS."
    fi
}

IMAGE_TYPEDEP_garagesign = "ostreepush"
do_image_garagesign[depends] += "unzip-native:do_populate_sysroot"
IMAGE_CMD_garagesign () {
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        # if credentials are issued by a server that doesn't support offline signing, exit silently
        unzip -p ${SOTA_PACKED_CREDENTIALS} root.json targets.pub targets.sec tufrepo.url 2>&1 >/dev/null || exit 0

        java_version=$( java -version 2>&1 | awk -F '"' '/version/ {print $2}' )
        if [ "${java_version}" = "" ]; then
            bbfatal "Java is required for synchronization with update backend, but is not installed on the host machine"
        elif [ "${java_version}" \< "1.8" ]; then
            bbfatal "Java version >= 8 is required for synchronization with update backend"
        fi

        rm -rf ${GARAGE_SIGN_REPO}
        garage-sign init --repo tufrepo \
                         --home-dir ${GARAGE_SIGN_REPO} \
                         --credentials ${SOTA_PACKED_CREDENTIALS}

        ostree_target_hash=$(cat ${OSTREE_REPO}/refs/heads/${OSTREE_BRANCHNAME})

        # Use OSTree target hash as version if none was provided by the user
        target_version=${ostree_target_hash}
        if [ -n "${GARAGE_TARGET_VERSION}" ]; then
            target_version=${GARAGE_TARGET_VERSION}
            bbwarn "Target version is overriden with GARAGE_TARGET_VERSION variable. It is a dangerous operation, make sure you've read the respective secion in meta-updater/README.adoc"
        elif [ -e "${STAGING_DATADIR_NATIVE}/target_version" ]; then
            target_version=$(cat "${STAGING_DATADIR_NATIVE}/target_version")
            bbwarn "Target version is overriden with target_version file. It is a dangerous operation, make sure you've read the respective secion in meta-updater/README.adoc"
        fi

        # Push may fail due to race condition when multiple build machines try to push simultaneously
        #   in which case targets.json should be pulled again and the whole procedure repeated
        push_success=0
        for push_retries in $( seq 3 ); do
            garage-sign targets pull --repo tufrepo \
                                     --home-dir ${GARAGE_SIGN_REPO}
            garage-sign targets add --repo tufrepo \
                                    --home-dir ${GARAGE_SIGN_REPO} \
                                    --name ${GARAGE_TARGET_NAME} \
                                    --format OSTREE \
                                    --version ${target_version} \
                                    --length 0 \
                                    --url "${GARAGE_TARGET_URL}" \
                                    --sha256 ${ostree_target_hash} \
                                    --hardwareids ${SOTA_HARDWARE_ID}
            garage-sign targets sign --repo tufrepo \
                                     --home-dir ${GARAGE_SIGN_REPO} \
                                     --key-name=targets
            errcode=0
            garage-sign targets push --repo tufrepo \
                                     --home-dir ${GARAGE_SIGN_REPO} || errcode=$?
            if [ "$errcode" -eq "0" ]; then
                push_success=1
                break
            else
                bbwarn "Push to garage repository has failed, retrying"
            fi
        done
        rm -rf ${GARAGE_SIGN_REPO}

        if [ "$push_success" -ne "1" ]; then
            bbfatal "Couldn't push to garage repository"
        fi
    fi
}

IMAGE_TYPEDEP_garagecheck = "garagesign"
IMAGE_CMD_garagecheck () {
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        # if credentials are issued by a server that doesn't support offline signing, exit silently
        unzip -p ${SOTA_PACKED_CREDENTIALS} root.json targets.pub targets.sec tufrepo.url 2>&1 >/dev/null || exit 0
        ostree_target_hash=$(cat ${OSTREE_REPO}/refs/heads/${OSTREE_BRANCHNAME})

        garage-check --ref=${ostree_target_hash} \
                     --credentials=${SOTA_PACKED_CREDENTIALS} \
                     --cacert=${STAGING_ETCDIR_NATIVE}/ssl/certs/ca-certificates.crt
    fi
}
# vim:set ts=4 sw=4 sts=4 expandtab:
