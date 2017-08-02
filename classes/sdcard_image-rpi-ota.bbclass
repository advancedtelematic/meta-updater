inherit image_types
inherit linux-raspberrypi-base

#
# Create an image that can by written onto a SD card using dd.
#
# The disk layout used is:
#
#    0                      -> IMAGE_ROOTFS_ALIGNMENT         - reserved for other data
#    IMAGE_ROOTFS_ALIGNMENT -> BOOT_SPACE                     - bootloader and kernel
#    BOOT_SPACE             -> SDIMG_OTA_SIZE                 - rootfs
#

#                                                     Default Free space = 1.3x
#                                                     Use IMAGE_OVERHEAD_FACTOR to add more space
#                                                     <--------->
#            4MiB              40MiB           SDIMG_OTA_ROOTFS
# <-----------------------> <----------> <---------------------->
#  ------------------------ ------------ ------------------------
# | IMAGE_ROOTFS_ALIGNMENT | BOOT_SPACE | OTAROOT_SIZE           |
#  ------------------------ ------------ ------------------------
# ^                        ^            ^                        ^
# |                        |            |                        |
# 0                      4MiB     4MiB + 40MiB       4MiB + 40Mib + SDIMG_OTA_ROOTFS

# This image depends on the rootfs image
IMAGE_TYPEDEP_rpi-sdimg-ota = "${SDIMG_OTA_ROOTFS_TYPE}"

# Set kernel and boot loader
IMAGE_BOOTLOADER ?= "bcm2835-bootfiles"

# Set initramfs extension
KERNEL_INITRAMFS ?= ""

# Kernel image name
SDIMG_OTA_KERNELIMAGE_raspberrypi  ?= "kernel.img"
SDIMG_OTA_KERNELIMAGE_raspberrypi2 ?= "kernel7.img"
SDIMG_OTA_KERNELIMAGE_raspberrypi3 ?= "kernel7.img"

# Boot partition volume id
BOOTDD_VOLUME_ID ?= "${MACHINE}"

# Boot partition size [in KiB] (will be rounded up to IMAGE_ROOTFS_ALIGNMENT)
BOOT_SPACE ?= "40960"

# Set alignment to 4MB [in KiB]
IMAGE_ROOTFS_ALIGNMENT = "4096"

# Use an uncompressed ext3 by default as rootfs
SDIMG_OTA_ROOTFS_TYPE ?= "otaimg"
SDIMG_OTA_ROOTFS = "${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${SDIMG_OTA_ROOTFS_TYPE}"

IMAGE_DEPENDS_rpi-sdimg-ota = " \
			parted-native \
			mtools-native \
			dosfstools-native \
			virtual/kernel:do_deploy \
			${IMAGE_BOOTLOADER} \
			u-boot \
			"
IMAGE_TYPEDEP_rpi-sdimg-ota = "otaimg"

# SD card image name
SDIMG_OTA = "${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.rpi-sdimg-ota"

# Compression method to apply to SDIMG_OTA after it has been created. Supported
# compression formats are "gzip", "bzip2" or "xz". The original .rpi-sdimg-ota file
# is kept and a new compressed file is created if one of these compression
# formats is chosen. If SDIMG_OTA_COMPRESSION is set to any other value it is
# silently ignored.
#SDIMG_OTA_COMPRESSION ?= ""

# Additional files and/or directories to be copied into the vfat partition from the IMAGE_ROOTFS.
FATPAYLOAD ?= ""

IMAGEDATESTAMP = "${@time.strftime('%Y.%m.%d',time.gmtime())}"
IMAGE_CMD_rpi-sdimg-ota[vardepsexclude] += "IMAGEDATESTAMP"
IMAGE_CMD_rpi-sdimg-ota[vardepsexclude] += "DATETIME"

IMAGE_CMD_rpi-sdimg-ota () {

	# Align partitions
	OTAROOT_SIZE=`du -Lb ${SDIMG_OTA_ROOTFS} | cut -f1`
	OTAROOT_SIZE=$(expr ${OTAROOT_SIZE} / 1024 + 1)
	BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE} + ${IMAGE_ROOTFS_ALIGNMENT} - 1)
	BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE_ALIGNED} - ${BOOT_SPACE_ALIGNED} % ${IMAGE_ROOTFS_ALIGNMENT})
	SDIMG_OTA_SIZE=$(expr ${IMAGE_ROOTFS_ALIGNMENT} + ${BOOT_SPACE_ALIGNED} + $OTAROOT_SIZE)

	echo "Creating filesystem with Boot partition ${BOOT_SPACE_ALIGNED} KiB and RootFS $OTAROOT_SIZE KiB"

	# Check if we are building with device tree support
	DTS="${@get_dts(d, None)}"

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

	mcopy -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/bcm2835-bootfiles/* ::/
	
	if test -n "${DTS}"; then
		# Device Tree Overlays are assumed to be suffixed by '-overlay.dtb' string and will be put in a dedicated folder
		DT_OVERLAYS="${@split_overlays(d, 0)}"
		DT_ROOT="${@split_overlays(d, 1)}"

		# Copy board device trees to root folder
		for DTB in ${DT_ROOT}; do
			DTB_BASE_NAME=`basename ${DTB} .dtb`

			mcopy -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${DTB_BASE_NAME}.dtb ::${DTB_BASE_NAME}.dtb
		done

		# Copy device tree overlays to dedicated folder
		mmd -i ${WORKDIR}/boot.img overlays
		for DTB in ${DT_OVERLAYS}; do
			DTB_EXT=${DTB##*.}
			DTB_BASE_NAME=`basename ${DTB} ."${DTB_EXT}"`

			mcopy -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${DTB_BASE_NAME}.${DTB_EXT} ::overlays/${DTB_BASE_NAME}.${DTB_EXT}
		done
	fi

	case "${KERNEL_IMAGETYPE}" in
	"uImage")
		mcopy -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/u-boot.bin ::${SDIMG_OTA_KERNELIMAGE}
		;;
	*)
		bbfatal "Kernel uImage is required for OTA image. Please set KERNEL_IMAGETYPE to \"uImage\""
		;;
	esac

	if [ -n ${FATPAYLOAD} ] ; then
		echo "Copying payload into VFAT"
		for entry in ${FATPAYLOAD} ; do
				# add the || true to stop aborting on vfat issues like not supporting .~lock files
				mcopy -i ${WORKDIR}/boot.img -s -v ${IMAGE_ROOTFS}$entry :: || true
		done
	fi

	# Add stamp file
	echo "${IMAGE_NAME}-${IMAGEDATESTAMP}" > ${WORKDIR}/image-version-info
	mcopy -i ${WORKDIR}/boot.img -v ${WORKDIR}//image-version-info ::

	# Burn Partitions
	sync
	dd if=${WORKDIR}/boot.img of=${SDIMG_OTA} conv=notrunc seek=1 bs=$(expr ${IMAGE_ROOTFS_ALIGNMENT} \* 1024) && sync && sync
	# If SDIMG_OTA_ROOTFS_TYPE is a .xz file use xzcat
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

ROOTFS_POSTPROCESS_COMMAND += " rpi_generate_sysctl_config ; "

rpi_generate_sysctl_config() {
	# systemd sysctl config
	test -d ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d && \
		echo "vm.min_free_kbytes = 8192" > ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d/rpi-vm.conf

	# sysv sysctl config
	IMAGE_SYSCTL_CONF="${IMAGE_ROOTFS}${sysconfdir}/sysctl.conf"
	test -e ${IMAGE_ROOTFS}${sysconfdir}/sysctl.conf && \
		sed -e "/vm.min_free_kbytes/d" -i ${IMAGE_SYSCTL_CONF}
	echo "" >> ${IMAGE_SYSCTL_CONF} && echo "vm.min_free_kbytes = 8192" >> ${IMAGE_SYSCTL_CONF}
}
