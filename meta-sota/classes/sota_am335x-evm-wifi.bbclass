KERNEL_IMAGETYPE_sota = "uImage"

OSTREE_BOOTLOADER ?= "u-boot"

EXTRA_IMAGEDEPENDS_append_sota = " acer-bootfiles"
IMAGE_BOOT_FILES_sota = "bootfiles/*"
OSTREE_KERNEL_ARGS ?= "ramdisk_size=16384 root=/dev/ram0 rw rootfstype=ext4 rootwait rootdelay=2 ostree_root=/dev/mmcblk0p2 console=ttyO0,115200n8l"

IMAGE_INSTALL_append_sota = " uim iw wl18xx-calibrator wlconf wl18xx-fw hostapd wpa-supplicant"
IMAGE_INSTALL_remove_sota = " connman connman-client"

PREFERRED_VERSION_linux-ti-staging_sota = "4.4.54+gitAUTOINC+ecd4eada6f"

KERNEL_EXTRA_ARGS_append_sota = " LOADADDR=${UBOOT_ENTRYPOINT}"

VIRTUAL-RUNTIME_net_manager_sota = "systemd"
