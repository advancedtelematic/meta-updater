MACHINE = "qemux86-64"

PREFERRED_VERSION_gcc-cross-${TARGET_ARCH} ?= "5.4%"
PREFERRED_VERSION_libgcc ?= "5.4%"
PREFERRED_VERSION_gcc-runtime ?= "5.4%"

# U-Boot support for SOTA
PREFERRED_PROVIDER_virtual/bootloader_sota = "u-boot-ota"
UBOOT_MACHINE = "qemu-x86_defconfig"
OSTREE_BOOTLOADER ?= "u-boot"

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/hda"
