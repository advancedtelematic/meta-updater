OSTREE_KERNEL = "${KERNEL_IMAGETYPE}-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"

OSTREE_BOOTLOADER ?= "u-boot"
INITRAMFS_FSTYPES = "cpio.gz"
PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot"

IMAGE_BOOT_FILES += "uEnv.txt"
IMAGE_BOOT_FILES:remove = "fitImage"

OSTREE_KERNEL_ARGS:sota ?= "earlycon=sbi console=ttySIF0 ramdisk_size=16384 root=/dev/ram0 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/mmcblk0p3"

WKS_FILE:sota = "freedom-u540-opensbi-sota.wks"
