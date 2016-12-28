IMAGE_INSTALL_append = " ostree rvi-sota-client"

# live image for OSTree-enabled systems
IMAGE_CLASSES_append = " image_types_ostree image_types_ota"
IMAGE_FSTYPES += "ostreepush otaimg"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
OSTREE_BRANCHNAME ?= "ota-${MACHINE}"
OSTREE_OSNAME ?= "poky"
OSTREE_INITRAMFS_IMAGE ?= "initramfs-ostree-image"

# Platform-specific configurations

## RaspberryPi
IMAGE_CLASSES_append_raspberrypi2 = " image_types_uboot sdcard_image-rpi-ota"
IMAGE_CLASSES_append_raspberrypi3 = " image_types_uboot sdcard_image-rpi-ota"
IMAGE_FSTYPES += "${@'rpi-sdimg-ota' if d.getVar('MACHINE', True).startswith('raspberrypi') else ''}"
IMAGE_FSTYPES_remove = "rpi-sdimg"

KERNEL_IMAGETYPE_raspberrypi2 = "uImage"
KERNEL_IMAGETYPE_raspberrypi3 = "uImage"

UBOOT_MACHINE_raspberrypi2 = "rpi_2_defconfig"
UBOOT_MACHINE_raspberrypi3 = "rpi_3_32b_defconfig"
PREFERRED_PROVIDER_virtual/bootloader_raspberrypi2 = "u-boot"
PREFERRED_PROVIDER_virtual/bootloader_raspberrypi3 = "u-boot"

# Some BSPs (e.g. meta-raspberrypi) use this variable to turn debug on/off
DISTRO_TYPE ?= "${@bb.utils.contains("IMAGE_FEATURES", "debug-tweaks", "debug", "release",d)}"

## Minnowboard
PREFERRED_PROVIDER_virtual/bootloader_intel-corei7-64 = "u-boot-ota"
UBOOT_MACHINE_intel-corei7-64 = "minnowmax_defconfig"
IMAGE_CLASSES_append_intel-corei7-64 = " sdcard_image-minnowboard-ota"
IMAGE_FSTYPES += "${@'minnowboard-sdimg-ota' if d.getVar('MACHINE', True).startswith('intel-corei7-64') else ''}"

## QEMU
PREFERRED_PROVIDER_virtual/bootloader_qemux86 = "u-boot-ota"
UBOOT_MACHINE_qemux86 = "qemu-x86_defconfig"
PREFERRED_PROVIDER_virtual/bootloader_qemux86-64= "u-boot-ota"
UBOOT_MACHINE_qemux86-64 = "qemu-x86_defconfig"

DISTROOVERRIDES_append = ":sota"
DISTRO_FEATURES_append = " sota"
