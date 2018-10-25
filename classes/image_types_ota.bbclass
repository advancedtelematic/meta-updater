# Image to use with u-boot as BIOS and OSTree deployment system

#inherit image_types

# Boot filesystem size in MiB
# OSTree updates may require some space on boot file system for
# boot scripts, kernel and initramfs images
#


do_image_ota_ext4[depends] += "e2fsprogs-native:do_populate_sysroot"

calculate_size () {
	BASE=$1
	SCALE=$2
	MIN=$3
	MAX=$4
	EXTRA=$5
	ALIGN=$6

	SIZE=`echo "$BASE * $SCALE" | bc -l`
	REM=`echo $SIZE | cut -d "." -f 2`
	SIZE=`echo $SIZE | cut -d "." -f 1`

	if [ -n "$REM" -o ! "$REM" -eq 0 ]; then
		SIZE=`expr $SIZE \+ 1`
	fi

	if [ "$SIZE" -lt "$MIN" ]; then
		SIZE=$MIN
	fi

	SIZE=`expr $SIZE \+ $EXTRA`
	SIZE=`expr $SIZE \+ $ALIGN \- 1`
	SIZE=`expr $SIZE \- $SIZE \% $ALIGN`

	if [ -n "$MAX" ]; then
		if [ "$SIZE" -gt "$MAX" ]; then
			return -1
		fi
	fi
	
	echo "${SIZE}"
}

export OSTREE_OSNAME
export OSTREE_BRANCHNAME
export OSTREE_REPO
export OSTREE_BOOTLOADER

export GARAGE_TARGET_NAME

export OTA_SYSROOT="${WORKDIR}/ota-sysroot"

## Common OTA image setup
fakeroot do_otasetup () {
	
	if [ -z "$OSTREE_REPO" ]; then
		bbfatal "OSTREE_REPO should be set in your local.conf"
	fi

	if [ -z "$OSTREE_OSNAME" ]; then
		bbfatal "OSTREE_OSNAME should be set in your local.conf"
	fi

	if [ -z "$OSTREE_BRANCHNAME" ]; then
		bbfatal "OSTREE_BRANCHNAME should be set in your local.conf"
	fi

	# HaX! Since we are using a peristent directory, we need to be sure to clean it on run.
	mkdir -p ${OTA_SYSROOT}
	rm -rf ${OTA_SYSROOT}/*

	ostree admin --sysroot=${OTA_SYSROOT} init-fs ${OTA_SYSROOT}
	ostree admin --sysroot=${OTA_SYSROOT} os-init ${OSTREE_OSNAME}
	mkdir -p ${OTA_SYSROOT}/boot/loader.0
	ln -s loader.0 ${OTA_SYSROOT}/boot/loader

	if [ "${OSTREE_BOOTLOADER}" = "grub" ]; then
		mkdir -p ${OTA_SYSROOT}/boot/grub2
		ln -s ../loader/grub.cfg ${OTA_SYSROOT}/boot/grub2/grub.cfg
	elif [ "${OSTREE_BOOTLOADER}" = "u-boot" ]; then
		touch ${OTA_SYSROOT}/boot/loader/uEnv.txt
	else
		bberror "Invalid bootloader: ${OSTREE_BOOTLOADER}"
	fi;

	ostree_target_hash=$(cat ${OSTREE_REPO}/refs/heads/${OSTREE_BRANCHNAME})

	ostree --repo=${OTA_SYSROOT}/ostree/repo pull-local --remote=${OSTREE_OSNAME} ${OSTREE_REPO} ${ostree_target_hash}
	export OSTREE_BOOT_PARTITION="/boot"
	kargs_list=""
	for arg in ${OSTREE_KERNEL_ARGS}; do
		kargs_list="${kargs_list} --karg-append=$arg"
	done

	ostree admin --sysroot=${OTA_SYSROOT} deploy ${kargs_list} --os=${OSTREE_OSNAME} ${ostree_target_hash}

	# Copy deployment /home and /var/sota to sysroot
	HOME_TMP=`mktemp -d ${WORKDIR}/home-tmp-XXXXX`

	tar --xattrs --xattrs-include='*' -C ${HOME_TMP} -xf ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.rootfs.ostree.tar.bz2 ./usr/homedirs ./var/local || true

	cp -a ${IMAGE_ROOTFS}/var/sota ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/ || true
	# Create /var/sota if it doesn't exist yet
	mkdir -p ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota
	# Ensure the permissions are correctly set
	chmod 700 ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota

	mv ${HOME_TMP}/var/local ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/ || true
	mv ${HOME_TMP}/usr/homedirs/home ${OTA_SYSROOT}/ || true
	# Ensure that /var/local exists (AGL symlinks /usr/local to /var/local)
	install -d ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/local
	# Set package version for the first deployment
	target_version=${ostree_target_hash}
	if [ -n "${GARAGE_TARGET_VERSION}" ]; then
		target_version=${GARAGE_TARGET_VERSION}
	fi
	mkdir -p ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota/import
	echo "{\"${ostree_target_hash}\":\"${GARAGE_TARGET_NAME}-${target_version}\"}" > ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota/import/installed_versions
	echo "All done. Cleaning up dir: ${HOME_TMP}"
	rm -rf ${HOME_TMP}
}

IMAGE_CMD_ota-ext4 () {
	# Calculate image type
	OTA_ROOTFS_SIZE=$(calculate_size `du -ks $OTA_SYSROOT | cut -f 1`  "${IMAGE_OVERHEAD_FACTOR}" "${IMAGE_ROOTFS_SIZE}" "${IMAGE_ROOTFS_MAXSIZE}" `expr ${IMAGE_ROOTFS_EXTRA_SPACE}` "${IMAGE_ROOTFS_ALIGNMENT}")

	if [ $OTA_ROOTFS_SIZE -lt 0 ]; then
		bbfatal "create_ota failed to calculate OTA rootfs size!"
	fi

	eval local COUNT=\"0\"
	eval local MIN_COUNT=\"60\"
	if [ $OTA_ROOTFS_SIZE -lt $MIN_COUNT ]; then
		eval COUNT=\"$MIN_COUNT\"
	fi

	dd if=/dev/zero of=${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ota-ext4 seek=${OTA_ROOTFS_SIZE} count=$COUNT bs=1024
	mkfs.ext4 -O ^64bit ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ota-ext4 -L otaroot -d ${OTA_SYSROOT}
}

IMAGE_CMD_ota-tar () {
	tar -cf ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ota-tar -C ${OTA_SYSROOT} .
}

do_otasetup[doc] = "Sets up the base ota rootfs used for subsequent image generation"
do_otasetup[depends] += "virtual/fakeroot-native:do_populate_sysroot \
			${@'grub:do_populate_sysroot' if d.getVar('OSTREE_BOOTLOADER', True) == 'grub' else ''} \
			${@'virtual/bootloader:do_deploy' if d.getVar('OSTREE_BOOTLOADER', True) == 'u-boot' else ''}"

addtask do_otasetup after do_image_ostree before do_image_ota_ext4 do_image_ota_tar

IMAGE_TYPEDEP_ota-ext4 = "ostree"
IMAGE_TYPEDEP_ota-tar = "ostree"
