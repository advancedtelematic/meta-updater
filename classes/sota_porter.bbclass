# Commit united image to OSTree, not just uImage
OSTREE_KERNEL = "uImage+dtb"

EXTRA_IMAGEDEPENDS_append_sota = " porter-bootfiles"
IMAGE_BOOT_FILES_sota += "porter-bootfiles/*"

OSTREE_BOOTLOADER ?= "u-boot"
UBOOT_MACHINE_sota = "porter_config"

