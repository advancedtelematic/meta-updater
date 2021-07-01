# OSTree deployment
inherit features_check

REQUIRED_DISTRO_FEATURES = "usrmerge"

OSTREE_ROOTFS ??= "${WORKDIR}/ostree-rootfs"
OSTREE_COMMIT_SUBJECT ??= "Commit-id: ${IMAGE_NAME}"
OSTREE_COMMIT_BODY ??= ""
OSTREE_COMMIT_VERSION ??= "${DISTRO_VERSION}"
OSTREE_UPDATE_SUMMARY ??= "0"

BUILD_OSTREE_TARBALL ??= "1"

SYSTEMD_USED = "${@oe.utils.ifelse(d.getVar('VIRTUAL-RUNTIME_init_manager') == 'systemd', 'true', '')}"

IMAGE_CMD_TAR = "tar --xattrs --xattrs-include=*"
CONVERSION_CMD_tar = "touch ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.${type}; ${IMAGE_CMD_TAR} --numeric-owner -cf ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.${type}.tar -C ${TAR_IMAGE_ROOTFS} . || [ $? -eq 1 ]"
CONVERSIONTYPES_append = " tar"

TAR_IMAGE_ROOTFS_task-image-ostree = "${OSTREE_ROOTFS}"

do_image_ostree[dirs] = "${OSTREE_ROOTFS}"
do_image_ostree[cleandirs] = "${OSTREE_ROOTFS}"
do_image_ostree[depends] = "coreutils-native:do_populate_sysroot virtual/kernel:do_deploy ${INITRAMFS_IMAGE}:do_image_complete"
IMAGE_CMD_ostree () {
    # Copy required as we change permissions on some files.
    tar --xattrs --xattrs-include='*' -cf - -S -C ${IMAGE_ROOTFS} -p . | tar --xattrs --xattrs-include='*' -xf - -C ${OSTREE_ROOTFS}

    for d in var/*; do
      if [ "${d}" != "var/local" ]; then
        rm -rf ${d}
      fi
    done

    # Create sysroot directory to which physical sysroot will be mounted
    mkdir sysroot
    ln -sf sysroot/ostree ostree

    mkdir -p usr/rootdirs

    mv etc usr/

    if [ -n "${SYSTEMD_USED}" ]; then
        mkdir -p usr/etc/tmpfiles.d
        tmpfiles_conf=usr/etc/tmpfiles.d/00ostree-tmpfiles.conf
        echo "d /var/rootdirs 0755 root root -" >>${tmpfiles_conf}
    else
        mkdir -p usr/etc/init.d
        tmpfiles_conf=usr/etc/init.d/tmpfiles.sh
        echo '#!/bin/sh' > ${tmpfiles_conf}
        echo "mkdir -p /var/rootdirs; chmod 755 /var/rootdirs" >> ${tmpfiles_conf}

        ln -s ../init.d/tmpfiles.sh usr/etc/rcS.d/S20tmpfiles.sh
    fi

    # Preserve OSTREE_BRANCHNAME for future information
    mkdir -p usr/share/sota/
    echo -n "${OSTREE_BRANCHNAME}" > usr/share/sota/branchname

    # home directories get copied from the OE root later to the final sysroot
    # Create a symlink to var/rootdirs/home to make sure the OSTree deployment
    # redirects /home to /var/rootdirs/home.
    rm -rf home/
    ln -sf var/rootdirs/home home

    # Move persistent directories to /var
    dirs="opt mnt media srv"

    for dir in ${dirs}; do
        if [ -d ${dir} ] && [ ! -L ${dir} ]; then
            if [ "$(ls -A $dir)" ]; then
                bbwarn "Data in /$dir directory is not preserved by OSTree. Consider moving it under /usr"
            fi
            rm -rf ${dir}
        fi

        if [ -n "${SYSTEMD_USED}" ]; then
            echo "d /var/rootdirs/${dir} 0755 root root -" >>${tmpfiles_conf}
        else
            echo "mkdir -p /var/rootdirs/${dir}; chmod 755 /var/rootdirs/${dir}" >>${tmpfiles_conf}
        fi
        ln -sf var/rootdirs/${dir} ${dir}
    done

    if [ -d root ] && [ ! -L root ]; then
        if [ "$(ls -A root)" ]; then
            bbfatal "Data in /root directory is not preserved by OSTree."
        fi

        if [ -n "${SYSTEMD_USED}" ]; then
            echo "d /var/roothome 0700 root root -" >>${tmpfiles_conf}
        else
            echo "mkdir -p /var/roothome; chmod 700 /var/roothome" >>${tmpfiles_conf}
        fi

        rm -rf root
        ln -sf var/roothome root
    fi

    if [ -d usr/local ] && [ ! -L usr/local ]; then
        if [ "$(ls -A usr/local)" ]; then
            bbfatal "Data in /usr/local directory is not preserved by OSTree."
        fi
        rm -rf usr/local
    fi

    if [ -n "${SYSTEMD_USED}" ]; then
        echo "d /var/usrlocal 0755 root root -" >>${tmpfiles_conf}
    else
        echo "mkdir -p /var/usrlocal; chmod 755 /var/usrlocal" >>${tmpfiles_conf}
    fi

    dirs="bin etc games include lib man sbin share src"

    for dir in ${dirs}; do
        if [ -n "${SYSTEMD_USED}" ]; then
            echo "d /var/usrlocal/${dir} 0755 root root -" >>${tmpfiles_conf}
        else
            echo "mkdir -p /var/usrlocal/${dir}; chmod 755 /var/usrlocal/${dir}" >>${tmpfiles_conf}
        fi
    done

    ln -sf ../var/usrlocal usr/local

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
    ostree_target_hash=$(ostree --repo=${OSTREE_REPO} commit \
           --tree=dir=${OSTREE_ROOTFS} \
           --skip-if-unchanged \
           --branch=${OSTREE_BRANCHNAME} \
           --subject="${OSTREE_COMMIT_SUBJECT}" \
           --body="${OSTREE_COMMIT_BODY}" \
           --add-metadata-string=version="${OSTREE_COMMIT_VERSION}" \
           ${EXTRA_OSTREE_COMMIT})

    echo $ostree_target_hash > ${WORKDIR}/ostree_manifest

    if [ ${@ oe.types.boolean('${OSTREE_UPDATE_SUMMARY}')} = True ]; then
        ostree --repo=${OSTREE_REPO} summary -u
    fi
}

IMAGE_TYPEDEP_ostreepush = "ostreecommit"
do_image_ostreepush[depends] += "aktualizr-native:do_populate_sysroot ca-certificates-native:do_populate_sysroot"
do_image_ostreepush[lockfiles] += "${OSTREE_REPO}/ostree.lock"
IMAGE_CMD_ostreepush () {
    # send a copy of the repo manifest to backend if available
    local SEND_MANIFEST=""
    # check if garage-push supports the --repo-manifest option before trying
    if $(garage-push --help | grep -q '^\s*--repo-manifest') && [ -f ${IMAGE_ROOTFS}${sysconfdir}/manifest.xml ]; then
        SEND_MANIFEST="--repo-manifest ${IMAGE_ROOTFS}${sysconfdir}/manifest.xml"
    fi

    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        if [ -e ${SOTA_PACKED_CREDENTIALS} ]; then
            garage-push --loglevel 0 --repo=${OSTREE_REPO} \
                        --ref=${OSTREE_BRANCHNAME} \
                        --credentials=${SOTA_PACKED_CREDENTIALS} \
                        --cacert=${STAGING_ETCDIR_NATIVE}/ssl/certs/ca-certificates.crt \
                        $SEND_MANIFEST
        else
            bbwarn "SOTA_PACKED_CREDENTIALS file does not exist."
        fi
    else
        bbwarn "SOTA_PACKED_CREDENTIALS not set. Please add SOTA_PACKED_CREDENTIALS."
    fi
}

IMAGE_TYPEDEP_garagesign = "ostreepush"
do_image_garagesign[depends] += "unzip-native:do_populate_sysroot"
# This lock solves OTA-1866, which is that removing GARAGE_SIGN_REPO while using
# garage-sign simultaneously for two images often causes problems.
do_image_garagesign[lockfiles] += "${DEPLOY_DIR_IMAGE}/garagesign.lock"
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

        ostree_target_hash=$(cat ${WORKDIR}/ostree_manifest)

        # Use OSTree target hash as version if none was provided by the user
        target_version=${ostree_target_hash}
        if [ -n "${GARAGE_TARGET_VERSION}" ]; then
            target_version=${GARAGE_TARGET_VERSION}
            bbwarn "Target version is overriden with GARAGE_TARGET_VERSION variable. This is a dangerous operation! See https://docs.ota.here.com/ota-client/latest/build-configuration.html#_overriding_target_version"
        elif [ -e "${STAGING_DATADIR_NATIVE}/target_version" ]; then
            target_version=$(cat "${STAGING_DATADIR_NATIVE}/target_version")
            bbwarn "Target version is overriden with target_version file. This is a dangerous operation! See https://docs.ota.here.com/ota-client/latest/build-configuration.html#_overriding_target_version"
        fi

        # Push may fail due to race condition when multiple build machines try to push simultaneously
        #   in which case targets.json should be pulled again and the whole procedure repeated
        push_success=0
        target_url=""
        if [ -n "${GARAGE_TARGET_URL}" ]; then
            target_url="--url ${GARAGE_TARGET_URL}"
        fi
        target_expiry=""
        if [ -n "${GARAGE_TARGET_EXPIRES}" ] && [ -n "${GARAGE_TARGET_EXPIRE_AFTER}" ]; then
            bbfatal "Both GARAGE_TARGET_EXPIRES and GARAGE_TARGET_EXPIRE_AFTER are set. Only one can be set at a time."
        elif [ -n "${GARAGE_TARGET_EXPIRES}" ]; then
            target_expiry="--expires ${GARAGE_TARGET_EXPIRES}"
        elif [ -n "${GARAGE_TARGET_EXPIRE_AFTER}" ]; then
            target_expiry="--expire-after ${GARAGE_TARGET_EXPIRE_AFTER}"
        else
            target_expiry="--expire-after 1M"
        fi

        for push_retries in $( seq 3 ); do
            garage-sign targets pull --repo tufrepo \
                                     --home-dir ${GARAGE_SIGN_REPO}
            garage-sign targets add --repo tufrepo \
                                    --home-dir ${GARAGE_SIGN_REPO} \
                                    --name ${GARAGE_TARGET_NAME} \
                                    --format OSTREE \
                                    --version ${target_version} \
                                    --length 0 \
                                    ${target_url} \
                                    --sha256 ${ostree_target_hash} \
                                    --hardwareids ${SOTA_HARDWARE_ID}
            if [ -n "${GARAGE_CUSTOMIZE_TARGET}" ]; then
                bbplain "Running command(${GARAGE_CUSTOMIZE_TARGET}) to customize target"
                ${GARAGE_CUSTOMIZE_TARGET} \
                    ${GARAGE_SIGN_REPO}/tufrepo/roles/unsigned/targets.json \
                    ${GARAGE_TARGET_NAME}-${target_version}
            fi
            garage-sign targets sign --repo tufrepo \
                                     --home-dir ${GARAGE_SIGN_REPO} \
                                     ${target_expiry} \
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

        ostree_target_hash=$(cat ${WORKDIR}/ostree_manifest)

        garage-check --ref=${ostree_target_hash} \
                     --credentials=${SOTA_PACKED_CREDENTIALS} \
                     --cacert=${STAGING_ETCDIR_NATIVE}/ssl/certs/ca-certificates.crt
    fi
}
# vim:set ts=4 sw=4 sts=4 expandtab:
