KERNEL_IMAGETYPE_sota = "uImage"
PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot"
UBOOT_MACHINE_raspberrypi2_sota ?= "rpi_2_defconfig"
UBOOT_MACHINE_raspberrypi3_sota ?= "rpi_3_32b_defconfig"

OSTREE_BOOTLOADER ?= "u-boot"

# OSTree puts its own boot.scr to bcm2835-bootfiles
IMAGE_BOOT_FILES_remove_sota += "boot.scr"
