# OSTree deployment

inherit image

IMAGE_DEPENDS_ostree = "ostree-native:do_populate_sysroot \
                        openssl-native:do_populate_sysroot \
                        coreutils-native:do_populate_sysroot \
                        virtual/kernel:do_deploy \
                        ${OSTREE_INITRAMFS_IMAGE}:do_image_complete"

export OSTREE_REPO
export OSTREE_BRANCHNAME

RAMDISK_EXT ?= ".ext4.gz"
RAMDISK_EXT_arm ?= ".ext4.gz.u-boot"

OSTREE_KERNEL ??= "${KERNEL_IMAGETYPE}"

export SYSTEMD_USED = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', '', d)}"

IMAGE_CMD_ostree () {
    if [ -z "$OSTREE_REPO" ]; then
        bbfatal "OSTREE_REPO should be set in your local.conf"
    fi

    if [ -z "$OSTREE_BRANCHNAME" ]; then
        bbfatal "OSTREE_BRANCHNAME should be set in your local.conf"
    fi

    OSTREE_ROOTFS=`mktemp -du ${WORKDIR}/ostree-root-XXXXX`
    cp -a ${IMAGE_ROOTFS} ${OSTREE_ROOTFS}
    chmod a+rx ${OSTREE_ROOTFS}
    sync

    cd ${OSTREE_ROOTFS}

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

    if [ -n "$SYSTEMD_USED" ]; then
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

            if [ -n "$SYSTEMD_USED" ]; then
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
            bberror "Data in /root directory is not preserved by OSTree."
        fi

        if [ -n "$SYSTEMD_USED" ]; then
            echo "d /var/roothome 0755 root root -" >>${tmpfiles_conf}
        else
            echo "mkdir -p /var/roothome; chown 755 /var/roothome" >>${tmpfiles_conf}
        fi

        rm -rf root
        ln -sf var/roothome root
    fi

    if [ -n "${SOTA_SECONDARY_ECUS}" ]; then
        cp ${SOTA_SECONDARY_ECUS} var/sota/ecus
    fi

    # Creating boot directories is required for "ostree admin deploy"

    mkdir -p boot/loader.0
    mkdir -p boot/loader.1
    ln -sf boot/loader.0 boot/loader

    checksum=`sha256sum ${DEPLOY_DIR_IMAGE}/${OSTREE_KERNEL} | cut -f 1 -d " "`

    cp ${DEPLOY_DIR_IMAGE}/${OSTREE_KERNEL} boot/vmlinuz-${checksum}
    cp ${DEPLOY_DIR_IMAGE}/${OSTREE_INITRAMFS_IMAGE}-${MACHINE}${RAMDISK_EXT} boot/initramfs-${checksum}

    # Copy image manifest
    cat ${IMAGE_MANIFEST} | cut -d " " -f1,3 > usr/package.manifest

    cd ${WORKDIR}

    # Create a tarball that can be then commited to OSTree repo
    OSTREE_TAR=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.ostree.tar.bz2
    tar -C ${OSTREE_ROOTFS} --xattrs --xattrs-include='*' -cjf ${OSTREE_TAR} .
    sync

    rm -f ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.rootfs.ostree.tar.bz2
    ln -s ${IMAGE_NAME}.rootfs.ostree.tar.bz2 ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.rootfs.ostree.tar.bz2

    if [ ! -d ${OSTREE_REPO} ]; then
        ostree --repo=${OSTREE_REPO} init --mode=archive-z2
    fi

    # Commit the result
    ostree --repo=${OSTREE_REPO} commit \
           --tree=dir=${OSTREE_ROOTFS} \
           --skip-if-unchanged \
           --branch=${OSTREE_BRANCHNAME} \
           --subject="Commit-id: ${IMAGE_NAME}"

    rm -rf ${OSTREE_ROOTFS}
}

IMAGE_TYPEDEP_ostreepush = "ostree"
IMAGE_DEPENDS_ostreepush = "aktualizr-native:do_populate_sysroot ca-certificates-native:do_populate_sysroot "
IMAGE_CMD_ostreepush () {
    # Print warnings if credetials are not set or if the file has not been found.
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        if [ -e ${SOTA_PACKED_CREDENTIALS} ]; then
            garage-push --repo=${OSTREE_REPO} \
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

# vim:set ts=4 sw=4 sts=4 expandtab:
