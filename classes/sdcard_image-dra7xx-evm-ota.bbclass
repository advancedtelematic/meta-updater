inherit image_types

# Boot partition volume id
BOOTDD_VOLUME_ID ?= "${MACHINE}"

# Boot partition size [in KiB] (will be rounded up to IMAGE_ROOTFS_ALIGNMENT)
BOOT_SPACE ?= "4096"

IMAGE_ROOTFS_ALIGNMENT = "4096"
SDIMG_OTA_ROOTFS_TYPE ?= "otaimg"
SDIMG_OTA_ROOTFS = "${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${SDIMG_OTA_ROOTFS_TYPE}"

IMAGE_TYPEDEP_dra7xx-evm-sdimg-ota = "${SDIMG_OTA_ROOTFS_TYPE}"
IMAGE_DEPENDS_dra7xx-evm-sdimg-ota = " \
			parted-native \
			mtools-native \
			dosfstools-native \
			"

SDIMG_OTA = "${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.dra7xx-evm-sdimg-ota"

IMAGE_CMD_dra7xx-evm-sdimg-ota () {
	OTAROOT_SIZE=`du -Lb ${SDIMG_OTA_ROOTFS} | cut -f1`
	OTAROOT_SIZE=$(expr ${OTAROOT_SIZE} / 1024 + 1)
	BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE} + ${IMAGE_ROOTFS_ALIGNMENT} - 1)
	BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE_ALIGNED} - ${BOOT_SPACE_ALIGNED} % ${IMAGE_ROOTFS_ALIGNMENT})
	SDIMG_OTA_SIZE=$(expr ${IMAGE_ROOTFS_ALIGNMENT} + ${BOOT_SPACE_ALIGNED} + $OTAROOT_SIZE)

	echo "Creating filesystem with Boot partition ${BOOT_SPACE_ALIGNED} KiB and RootFS $OTAROOT_SIZE KiB"

	# Initialize sdcard image file
	dd if=/dev/zero of=${SDIMG_OTA} bs=1024 count=0 seek=${SDIMG_OTA_SIZE}

	# Create partition table
	parted -s ${SDIMG_OTA} mklabel msdos
	# Create boot partition and mark it as bootable
	parted -s ${SDIMG_OTA} unit KiB mkpart primary fat32 ${IMAGE_ROOTFS_ALIGNMENT} $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT})
	parted -s ${SDIMG_OTA} set 1 boot on
	# Create rootfs partition to the end of disk
	parted -s ${SDIMG_OTA} -- unit KiB mkpart primary ext2 $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT}) -1s
	parted ${SDIMG_OTA} print

	# Create a vfat image with boot files
	BOOT_BLOCKS=$(LC_ALL=C parted -s ${SDIMG_OTA} unit b print | awk '/ 1 / { print substr($4, 1, length($4 -1)) / 512 /2 }')
	rm -f ${WORKDIR}/boot.img
	mkfs.vfat -n "${BOOTDD_VOLUME_ID}" -S 512 -C ${WORKDIR}/boot.img $BOOT_BLOCKS
	sync

	sync
	#dd if=${WORKDIR}/boot.img of=${SDIMG_OTA} conv=notrunc seek=1 bs=$(expr ${IMAGE_ROOTFS_ALIGNMENT} \* 1024) && sync && sync

	if echo "${SDIMG_OTA_ROOTFS_TYPE}" | egrep -q "*\.xz"
	then
		xzcat ${SDIMG_OTA_ROOTFS} | dd of=${SDIMG_OTA} conv=notrunc seek=1 bs=$(expr 1024 \* ${BOOT_SPACE_ALIGNED} + ${IMAGE_ROOTFS_ALIGNMENT} \* 1024) && sync && sync
	else
		dd if=${SDIMG_OTA_ROOTFS} of=${SDIMG_OTA} conv=notrunc seek=1 bs=$(expr 1024 \* ${BOOT_SPACE_ALIGNED} + ${IMAGE_ROOTFS_ALIGNMENT} \* 1024) && sync && sync
	fi

	# Optionally apply compression
	case "${SDIMG_OTA_COMPRESSION}" in
	"gzip")
		gzip -k9 "${SDIMG_OTA}"
		;;
	"bzip2")
		bzip2 -k9 "${SDIMG_OTA}"
		;;
	"xz")
		xz -k "${SDIMG_OTA}"
		;;
	esac
}

