IMAGE_FSTYPES:remove = "wic"

# U-Boot support for SOTA
PREFERRED_PROVIDER_virtual/bootloader_sota = "u-boot"
UBOOT_MACHINE:sota = "qemu-x86_defconfig"
OSTREE_BOOTLOADER ?= "u-boot"
INITRAMFS_FSTYPES ?= "cpio.gz"

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/hda"

IMAGE_ROOTFS_EXTRA_SPACE = "${@bb.utils.contains('DISTRO_FEATURES', 'sota', '65536', '', d)}"

# fix for u-boot/swig build issue
HOSTTOOLS_NONFATAL += "x86_64-linux-gnu-gcc"

IMAGE_INSTALL:append:sota = " network-configuration "
