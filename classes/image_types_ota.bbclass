# Image to use with u-boot as BIOS and OSTree deployment system

#inherit image_types

# Boot filesystem size in MiB
# OSTree updates may require some space on boot file system for
# boot scripts, kernel and initramfs images
#

do_image_otaimg[depends] += " \
			${@'grub:do_populate_sysroot' if d.getVar('OSTREE_BOOTLOADER', True) == 'grub' else ''} \
			${@'virtual/bootloader:do_deploy' if d.getVar('OSTREE_BOOTLOADER', True) == 'u-boot' else ''}"


export OSTREE_OSNAME
export OSTREE_BRANCHNAME
export OSTREE_REPO
export OSTREE_BOOTLOADER

export GARAGE_TARGET_NAME

IMAGE_CMD_otaimg () {
	if ${@bb.utils.contains('IMAGE_FSTYPES', 'otaimg', 'true', 'false', d)}; then
		if [ -z "$OSTREE_REPO" ]; then
			bbfatal "OSTREE_REPO should be set in your local.conf"
		fi

		if [ -z "$OSTREE_OSNAME" ]; then
			bbfatal "OSTREE_OSNAME should be set in your local.conf"
		fi

		if [ -z "$OSTREE_BRANCHNAME" ]; then
			bbfatal "OSTREE_BRANCHNAME should be set in your local.conf"
		fi


		PHYS_SYSROOT=${OSTREE_IMAGE_SYSROOT}
		rm -rf ${PHYS_SYSROOT} || true
		mkdir ${PHYS_SYSROOT}

		ostree admin --sysroot=${PHYS_SYSROOT} init-fs ${PHYS_SYSROOT}
		ostree admin --sysroot=${PHYS_SYSROOT} os-init ${OSTREE_OSNAME}

		mkdir -p ${PHYS_SYSROOT}/boot/loader.0
		ln -s loader.0 ${PHYS_SYSROOT}/boot/loader

		if [ "${OSTREE_BOOTLOADER}" = "grub" ]; then
			mkdir -p ${PHYS_SYSROOT}/boot/grub2
			ln -s ../loader/grub.cfg ${PHYS_SYSROOT}/boot/grub2/grub.cfg
		elif [ "${OSTREE_BOOTLOADER}" = "u-boot" ]; then
			touch ${PHYS_SYSROOT}/boot/loader/uEnv.txt
		else
			bberror "Invalid bootloader: ${OSTREE_BOOTLOADER}"
		fi;

		ostree_target_hash=$(cat ${OSTREE_REPO}/refs/heads/${OSTREE_BRANCHNAME})

		ostree --repo=${PHYS_SYSROOT}/ostree/repo pull-local --remote=${OSTREE_OSNAME} ${OSTREE_REPO} ${ostree_target_hash}
		export OSTREE_BOOT_PARTITION="/boot"
		kargs_list=""
		for arg in ${OSTREE_KERNEL_ARGS}; do
			kargs_list="${kargs_list} --karg-append=$arg"
		done

		ostree admin --sysroot=${PHYS_SYSROOT} deploy ${kargs_list} --os=${OSTREE_OSNAME} ${ostree_target_hash}

		# Copy deployment /home and /var/sota to sysroot
		HOME_TMP=`mktemp -d ${WORKDIR}/home-tmp-XXXXX`
		tar --xattrs --xattrs-include='*' -C ${HOME_TMP} -xf ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.rootfs.ostree.tar.bz2 ./usr/homedirs ./var/sota ./var/local || true
		mv ${HOME_TMP}/var/sota ${PHYS_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/ || true
		mv ${HOME_TMP}/var/local ${PHYS_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/ || true
		# Create /var/sota if it doesn't exist yet
		mkdir -p ${PHYS_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota || true
		# Ensure the permissions are correctly set
		chmod 700 ${PHYS_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota
		mv ${HOME_TMP}/usr/homedirs/home ${PHYS_SYSROOT}/ || true
		# Ensure that /var/local exists (AGL symlinks /usr/local to /var/local)
		install -d ${PHYS_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/local
		# Set package version for the first deployment
		target_version=${ostree_target_hash}
		if [ -n "${GARAGE_TARGET_VERSION}" ]; then
			target_version=${GARAGE_TARGET_VERSION}
		fi
		echo "{\"${ostree_target_hash}\":\"${GARAGE_TARGET_NAME}-${target_version}\"}" > ${PHYS_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota/installed_versions

		rm -rf ${HOME_TMP}

	fi
}

IMAGE_TYPEDEP_otaimg = "ostree"
