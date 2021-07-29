# Commit united image to OSTree, not just uImage
OSTREE_KERNEL = "uImage+dtb"

EXTRA_IMAGEDEPENDS:append:sota = " porter-bootfiles"
IMAGE_BOOT_FILES:sota += "porter-bootfiles/*"

OSTREE_BOOTLOADER ?= "u-boot"
UBOOT_MACHINE:sota = "porter_config"

PREFERRED_RPROVIDER_network-configuration ?= "connman"
IMAGE_INSTALL:append:sota = " network-configuration "
