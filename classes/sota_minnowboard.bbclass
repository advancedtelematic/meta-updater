OSTREE_BOOTLOADER ?= "grub"
EFI_PROVIDER_sota = "grub-efi"

WKS_FILE_sota = "efiimage-sota.wks"
IMAGE_BOOT_FILES_sota = ""

IMAGE_FSTYPES_remove_sota = "live hddimg"
OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 console=ttyS0,115200 console=tty0"
IMAGE_INSTALL_append = " minnowboard-efi-startup"

PREFERRED_RPROVIDER_network-configuration ?= "connman"
IMAGE_INSTALL_append_sota = " network-configuration "
