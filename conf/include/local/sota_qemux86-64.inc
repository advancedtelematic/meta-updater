MACHINE = "qemux86-64"

# U-Boot support for SOTA
PREFERRED_PROVIDER_virtual/bootloader_sota = "u-boot-ota"
UBOOT_MACHINE_sota = "qemu-x86_defconfig"
OSTREE_BOOTLOADER ?= "u-boot"

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/hda"
