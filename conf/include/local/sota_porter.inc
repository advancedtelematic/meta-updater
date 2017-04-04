MACHINE = "porter"

# Commit united image to OSTree, not just uImage
OSTREE_KERNEL = "uImage+dtb"

IMAGE_CLASSES_append = " image_types_uboot "
IMAGE_BOOT_FILES = "porter-bootfiles/*"

OSTREE_BOOTLOADER ?= "u-boot"
UBOOT_MACHINE = "porter_vin_config"

