# Image to use with u-boot as BIOS and OSTree deployment system

# Boot filesystem size in MiB
# OSTree updates may require some space on boot file system for
# boot scripts, kernel and initramfs images
#
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

OTA_SYSROOT = "${WORKDIR}/ota-sysroot"
TAR_IMAGE_ROOTFS_task-image-ota = "${OTA_SYSROOT}"
IMAGE_TYPEDEP_ota = "ostreecommit"
do_image_ota[dirs] = "${OTA_SYSROOT}"
do_image_ota[cleandirs] = "${OTA_SYSROOT}"
do_image_ota[depends] = "${@'grub:do_populate_sysroot' if d.getVar('OSTREE_BOOTLOADER') == 'grub' else ''} \
                         ${@'virtual/bootloader:do_deploy' if d.getVar('OSTREE_BOOTLOADER') == 'u-boot' else ''}"
IMAGE_CMD_ota () {
	ostree admin --sysroot=${OTA_SYSROOT} init-fs --modern ${OTA_SYSROOT}
	ostree admin --sysroot=${OTA_SYSROOT} os-init ${OSTREE_OSNAME}

	# Preparation required to steer ostree bootloader detection
	mkdir -p ${OTA_SYSROOT}/boot/loader.0
	ln -s loader.0 ${OTA_SYSROOT}/boot/loader

	if [ "${OSTREE_BOOTLOADER}" = "grub" ]; then
		# Used by ostree-grub-generator called by the ostree binary
		export OSTREE_BOOT_PARTITION=${OSTREE_BOOT_PARTITION}

		mkdir -p ${OTA_SYSROOT}/boot/grub2
		ln -s ../loader/grub.cfg ${OTA_SYSROOT}/boot/grub2/grub.cfg
	elif [ "${OSTREE_BOOTLOADER}" = "u-boot" ]; then
		touch ${OTA_SYSROOT}/boot/loader/uEnv.txt
	else
		bbfatal "Invalid bootloader: ${OSTREE_BOOTLOADER}"
	fi

	ostree_target_hash=$(cat ${WORKDIR}/ostree_manifest)

	# Use OSTree hash to avoid any potential race conditions between
	# multiple builds accessing the same ${OSTREE_REPO}.
	ostree --repo=${OTA_SYSROOT}/ostree/repo pull-local --remote=${OSTREE_OSNAME} ${OSTREE_REPO} ${ostree_target_hash}
	kargs_list=""
	for arg in ${OSTREE_KERNEL_ARGS}; do
		kargs_list="${kargs_list} --karg-append=$arg"
	done

	# Create the same reference on the device we use in the archive OSTree
	# repo in ${OSTREE_REPO}. This reference will show up when showing the
	# deployment on the device:
	# ostree admin status
	# If a remote with the name ${OSTREE_OSNAME} is configured, this also
	# will allow to use:
	# ostree admin upgrade
	ostree --repo=${OTA_SYSROOT}/ostree/repo refs --create=${OSTREE_OSNAME}:${OSTREE_BRANCHNAME} ${ostree_target_hash}
	ostree admin --sysroot=${OTA_SYSROOT} deploy ${kargs_list} --os=${OSTREE_OSNAME} ${OSTREE_OSNAME}:${OSTREE_BRANCHNAME}

	cp -a ${IMAGE_ROOTFS}/var/sota ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/ || true
	# Create /var/sota if it doesn't exist yet
	mkdir -p ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota
	# Ensure the permissions are correctly set
	chmod 700 ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota

	cp -a ${IMAGE_ROOTFS}/var/local ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/ || true

	mkdir -p ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/rootdirs
	cp -a ${IMAGE_ROOTFS}/home ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/rootdirs/home || true

	# Ensure that /var/local exists (AGL symlinks /usr/local to /var/local)
	install -d ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/local
	# Set package version for the first deployment
	target_version=${ostree_target_hash}
	if [ -n "${GARAGE_TARGET_VERSION}" ]; then
		target_version=${GARAGE_TARGET_VERSION}
	elif [ -e "${STAGING_DATADIR_NATIVE}/target_version" ]; then
		target_version=$(cat "${STAGING_DATADIR_NATIVE}/target_version")
	fi
	mkdir -p ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota/import
	echo "{\"${ostree_target_hash}\":\"${GARAGE_TARGET_NAME}-${target_version}\"}" > ${OTA_SYSROOT}/ostree/deploy/${OSTREE_OSNAME}/var/sota/import/installed_versions
}

IMAGE_TYPEDEP_ota-ext4 = "ota"
do_image_ota_ext4[depends] = "e2fsprogs-native:do_populate_sysroot"
IMAGE_CMD_ota-ext4 () {
	# Calculate image size
	OTA_ROOTFS_SIZE=$(calculate_size `du -ks ${OTA_SYSROOT} | cut -f 1`  "${IMAGE_OVERHEAD_FACTOR}" "${IMAGE_ROOTFS_SIZE}" "${IMAGE_ROOTFS_MAXSIZE}" `expr ${IMAGE_ROOTFS_EXTRA_SPACE}` "${IMAGE_ROOTFS_ALIGNMENT}")

	if [ ${OTA_ROOTFS_SIZE} -lt 0 ]; then
		bbfatal "create_ota failed to calculate OTA rootfs size!"
	fi

	eval local COUNT=\"0\"
	eval local MIN_COUNT=\"60\"
	if [ ${OTA_ROOTFS_SIZE} -lt ${MIN_COUNT} ]; then
		eval COUNT=\"${MIN_COUNT}\"
	fi

	dd if=/dev/zero of=${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ota-ext4 seek=${OTA_ROOTFS_SIZE} count=${COUNT} bs=1024
	mkfs.ext4 -O ^64bit ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ota-ext4 -L otaroot -d ${OTA_SYSROOT}
}

do_image_wic[depends] += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', '%s:do_image_ota_ext4' % d.getVar('PN'), '', d)}"
