IMAGE_CLASSES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'image_types_uboot sdcard_image-rpi-ota', '', d)}"
IMAGE_FSTYPES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'rpi-sdimg-ota', 'rpi-sdimg', d)}"

### both rpi-sdimg and rpi-sdimg-ota broken
IMAGE_FSTYPES += "ext4.xz ext4.bmap tar.xz"

KERNEL_IMAGETYPE_sota = "uImage"
PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot"
UBOOT_MACHINE_raspberrypi2_sota ?= "rpi_2_defconfig"
UBOOT_MACHINE_raspberrypi3_sota ?= "rpi_3_32b_defconfig"

OSTREE_BOOTLOADER ?= "u-boot"
