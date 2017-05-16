# See https://advancedtelematic.atlassian.net/browse/PRO-2693
PREFERRED_VERSION_linux-yocto_qemux86-64_sota = "4.4%"

IMAGE_FSTYPES_remove = "wic"

# U-Boot support for SOTA
PREFERRED_PROVIDER_virtual/bootloader_sota = "u-boot-ota"
UBOOT_MACHINE_sota = "qemu-x86_defconfig"
OSTREE_BOOTLOADER ?= "u-boot"

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/hda"
