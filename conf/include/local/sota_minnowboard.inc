MACHINE = "intel-corei7-64"

PREFERRED_PROVIDER_virtual/bootloader = "u-boot-ota"
UBOOT_MACHINE = "minnowmax_defconfig"

EXTRA_IMAGEDEPENDS_append = " minnowboard-bootfiles"
IMAGE_BOOT_FILES = "minnowboard-bootfiles/*"

