IMAGE_FSTYPES_remove = "wic"

# U-Boot support for SOTA
PREFERRED_PROVIDER_virtual/bootloader_sota = "u-boot"
UBOOT_MACHINE_sota = "qemu-x86_defconfig"
OSTREE_BOOTLOADER ?= "u-boot"

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/hda"

IMAGE_ROOTFS_EXTRA_SPACE = "${@bb.utils.contains('DISTRO_FEATURES', 'sota', '65536', '', d)}"
