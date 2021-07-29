KERNEL_IMAGETYPE:sota = "uImage"

OSTREE_BOOTLOADER ?= "u-boot"

EXTRA_IMAGEDEPENDS:append:sota = " acer-bootfiles"
IMAGE_BOOT_FILES:sota = "bootfiles/*"
OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 root=/dev/ram0 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/mmcblk0p2 console=ttyO0,115200n8l"

IMAGE_INSTALL:append:sota = " uim iw wl18xx-calibrator wlconf wl18xx-fw hostapd wpa-supplicant"

PREFERRED_VERSION_linux-ti-staging:sota = "4.4.54+gitAUTOINC+ecd4eada6f"

KERNEL_EXTRA_ARGS:append:sota = " LOADADDR=${UBOOT_ENTRYPOINT}"

VIRTUAL-RUNTIME_net_manager:sota = "systemd"
