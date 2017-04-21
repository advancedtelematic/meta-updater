MACHINE = "intel-corei7-64"

OSTREE_BOOTLOADER ?= "grub"
EFI_PROVIDER = "grub-efi"

WKS_FILE = "efiimage-sota.wks"
IMAGE_BOOT_FILES = ""

OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 rw rootfstype=ext4 rootwait rootdelay=2 console=ttyS0,115200 console=tty0"
