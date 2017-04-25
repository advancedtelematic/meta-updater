PREFERRED_PROVIDER_virtual/bootloader = "u-boot-ota"
UBOOT_MACHINE = "minnowmax_defconfig"

EXTRA_IMAGEDEPENDS_append = " minnowboard-bootfiles"
IMAGE_BOOT_FILES = "minnowboard-bootfiles/*"

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/hda console=ttyS0,115200 console=tty0"

