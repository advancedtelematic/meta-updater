OSTREE_BOOTLOADER ?= "u-boot"

IMAGE_BOOT_FILES = "MLO u-boot.img uEnv.txt"

KERNEL_IMAGETYPES_sota = "fitImage"

OSTREE_KERNEL = "fitImage"
OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 root=/dev/ram0 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/mmcblk0p2 console=ttyO0,115200n8l"

