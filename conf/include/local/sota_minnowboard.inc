MACHINE = "intel-corei7-64"

PREFERRED_PROVIDER_virtual/bootloader = "u-boot-ota"
UBOOT_MACHINE = "minnowmax_defconfig"
IMAGE_CLASSES_append = " sdcard_image-minnowboard-ota"
IMAGE_FSTYPES += "minnowboard-sdimg-ota"
