IMAGE_BOOT_FILES = "tiboot3.bin tispl.bin sysfw.itb u-boot.img uEnv.txt"

KERNEL_IMAGETYPES = "fitImage"
OPTEEFLAVOR = ""

OSTREE_KERNEL = "fitImage"
OSTREE_KERNEL_ARGS = "ramdisk_size=8192 root=/dev/ram0 rw rootfstype=ext4 ostree_root=/dev/mmcblk1p2 module_blacklist=sa2ul"

UBOOT_ENTRYPOINT = "0x80080000"
UBOOT_LOADADDRESS = "0x80080000"
