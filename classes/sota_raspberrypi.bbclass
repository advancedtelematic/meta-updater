IMAGE_CLASSES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'image_types_uboot sdcard_image-rpi-ota', '', d)}"
IMAGE_FSTYPES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'rpi-sdimg-ota.xz', 'rpi-sdimg.xz', d)}"

IMAGE_FSTYPES_remove = "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'wic rpi-sdimg', '', d)}"

KERNEL_IMAGETYPE_sota = "uImage"
PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot"
UBOOT_MACHINE_raspberrypi2_sota ?= "rpi_2_defconfig"
UBOOT_MACHINE_raspberrypi3_sota ?= "rpi_3_32b_defconfig"

OSTREE_BOOTLOADER ?= "u-boot"
