MACHINE ?= "raspberrypi3"

# normal image class is sdcard_image-rpi-gdp, for sota it is image_types_uboot & sdcard_image-rpi-ota
IMAGE_CLASSES += "image_types_uboot sdcard_image-rpi-ota"

# normal image is rpi-sdimg, for sota it is rpi-sdimg-ota
IMAGE_FSTYPES += "rpi-sdimg-ota"
### both rpi-sdimg and rpi-sdimg-ota broken
IMAGE_FSTYPES += "ext4.xz ext4.bmap tar.xz"

KERNEL_IMAGETYPE_sota = "uImage"
PREFERRED_PROVIDER_virtual/bootloader = "u-boot"
UBOOT_MACHINE_raspberrypi2 = "rpi_2_defconfig"
UBOOT_MACHINE_raspberrypi3 = "rpi_3_32b_defconfig"

OSTREE_BOOTLOADER ?= "u-boot"
