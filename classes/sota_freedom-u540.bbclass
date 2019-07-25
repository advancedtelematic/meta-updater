# Support fitImage by default
KERNEL_CLASSES_sota = " kernel-fitimage "
KERNEL_IMAGETYPE_sota = "fitImage"
KERNEL_IMAGETYPES_remove_sota = "uImage"
OSTREE_KERNEL = "${KERNEL_IMAGETYPE}-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"

OSTREE_BOOTLOADER ?= "u-boot"
INITRAMFS_FSTYPES = "cpio.gz"
PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot"

IMAGE_BOOT_FILES_sota ?= "fw_payload.bin boot.scr uEnv.txt"
KERNEL_DEVICETREE_sota ?= "sifive/${RISCV_SBI_FDT}"

OSTREE_KERNEL_ARGS_sota ?= "earlycon=sbi console=ttySIF0 ramdisk_size=16384 root=/dev/ram0 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/mmcblk0p3"

WKS_FILE_sota = "freedom-u540-opensbi-sota.wks"
