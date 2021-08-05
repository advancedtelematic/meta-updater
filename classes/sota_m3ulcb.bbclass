# Commit united image to OSTree, not just uImage
OSTREE_KERNEL = "Image"

EXTRA_IMAGEDEPENDS:append:sota = " renesas-ota-bootfiles"
IMAGE_BOOT_FILES:sota += "renesas-ota-bootfiles/*"

OSTREE_BOOTLOADER ?= "u-boot"

UBOOT_MACHINE:sota = "${@d.getVar('SOC_FAMILY').split(':')[1]}_ulcb_defconfig"

PREFERRED_RPROVIDER_network-configuration ?= "connman"
IMAGE_INSTALL:append:sota = " network-configuration "
