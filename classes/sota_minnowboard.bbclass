OSTREE_BOOTLOADER ?= "grub"
EFI_PROVIDER:sota = "grub-efi"

WKS_FILE:sota = "efiimage-sota.wks"
IMAGE_BOOT_FILES:sota = ""

IMAGE_FSTYPES:remove:sota = "live hddimg"
OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 console=ttyS0,115200 console=tty0"
IMAGE_INSTALL:append = " minnowboard-efi-startup"

PREFERRED_RPROVIDER_network-configuration ?= "connman"
IMAGE_INSTALL:append:sota = " network-configuration "
