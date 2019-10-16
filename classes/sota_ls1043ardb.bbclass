KERNEL_IMAGETYPES = "fitImage"

OSTREE_KERNEL = "fitImage-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
OSTREE_KERNEL_ARGS = "console=ttyS0,115200 ramdisk_size=8192 root=/dev/ram0 rw rootfstype=ext4 ostree_root=/dev/mmcblk0p2"

WKS_FILE_sota = "ls1043ardb-ota.wks"
IMAGE_BOOT_FILES = "ls1043ardb_boot.scr"
