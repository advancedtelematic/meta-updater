# Commit united image to OSTree, not just uImage
OSTREE_KERNEL = "Image"

EXTRA_IMAGEDEPENDS_append_sota = " m3ulcb-ota-bootfiles"
IMAGE_BOOT_FILES_sota += "m3ulcb-ota-bootfiles/*"

OSTREE_BOOTLOADER ?= "u-boot"
UBOOT_MACHINE_sota = "m3ulcb_defconfig"
