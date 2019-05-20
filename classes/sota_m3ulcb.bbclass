# Commit united image to OSTree, not just uImage
OSTREE_KERNEL = "Image"

EXTRA_IMAGEDEPENDS_append_sota = " renesas-ota-bootfiles"
IMAGE_BOOT_FILES_sota += "renesas-ota-bootfiles/*"

OSTREE_BOOTLOADER ?= "u-boot"
UBOOT_MACHINE_sota = "m3ulcb_defconfig"

PREFERRED_RPROVIDER_virtual/network-configuration ?= "connman"
IMAGE_INSTALL_append_sota = " virtual/network-configuration "
