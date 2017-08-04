PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot-ota"
UBOOT_MACHINE_sota ?= "minnowmax_defconfig"

EXTRA_IMAGEDEPENDS_append_sota = " minnowboard-bootfiles"
IMAGE_BOOT_FILES_sota ?= "minnowboard-bootfiles/*"

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/hda console=ttyS0,115200 console=tty0"

