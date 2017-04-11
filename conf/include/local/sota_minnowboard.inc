MACHINE = "intel-corei7-64"

PREFERRED_PROVIDER_virtual/bootloader_sota = "u-boot-ota"
UBOOT_MACHINE_sota = "minnowmax_defconfig"

EXTRA_IMAGEDEPENDS_append_sota = " minnowboard-bootfiles"
IMAGE_BOOT_FILES_sota += "minnowboard-bootfiles/*"

OSTREE_BOOTLOADER ?= "u-boot"
