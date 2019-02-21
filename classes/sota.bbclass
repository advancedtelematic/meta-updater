OVERRIDES .= "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ':sota', '', d)}"

HOSTTOOLS_NONFATAL += "java"

SOTA_CLIENT ??= "aktualizr"
SOTA_CLIENT_PROV ??= "aktualizr-auto-prov"
SOTA_DEPLOY_CREDENTIALS ?= "1"
SOTA_HARDWARE_ID ??= "${MACHINE}"

IMAGE_INSTALL_append_sota = " ostree os-release ${SOTA_CLIENT} ${SOTA_CLIENT_PROV}"
IMAGE_CLASSES += " image_types_ostree image_types_ota"

IMAGE_FSTYPES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'ostreepush garagesign garagecheck ota-ext4 wic', ' ', d)}"
IMAGE_FSTYPES += "${@bb.utils.contains('BUILD_OSTREE_TARBALL', '1', 'ostree.tar.bz2', ' ', d)}"
IMAGE_FSTYPES += "${@bb.utils.contains('BUILD_OTA_TARBALL', '1', 'ota.tar.xz', ' ', d)}"

PACKAGECONFIG_append_pn-curl = " ssl"
PACKAGECONFIG_remove_pn-curl = "gnutls"

WKS_FILE_sota ?= "sdimage-sota.wks"

EXTRA_IMAGEDEPENDS_append_sota = " parted-native mtools-native dosfstools-native"

# Has to be an override, because 'INITRAMFS_FSTYPES ?=' is not strong enough to override the default value
INITRAMFS_FSTYPES_sota ?= "${@oe.utils.ifelse(d.getVar('OSTREE_BOOTLOADER') == 'u-boot' and d.getVar('KERNEL_IMAGETYPE') != 'fitImage', 'cpio.gz.u-boot', 'cpio.gz')}"

# Deploy config fragment list to OSTree root fs
IMAGE_INSTALL_append = "${@oe.utils.ifelse(d.getVar('KERNEL_IMAGETYPE') == 'fitImage', ' fit-conf', ' ')}"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
export OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
export OSTREE_BRANCHNAME ?= "${SOTA_HARDWARE_ID}"
export OSTREE_OSNAME ?= "poky"
export OSTREE_BOOTLOADER ??= 'u-boot'
export OSTREE_BOOT_PARTITION ??= "/boot"

INITRAMFS_IMAGE ?= "initramfs-ostree-image"

GARAGE_SIGN_REPO ?= "${DEPLOY_DIR_IMAGE}/garage_sign_repo"
GARAGE_SIGN_KEYNAME ?= "garage-key"
GARAGE_TARGET_NAME ?= "${OSTREE_BRANCHNAME}"
GARAGE_TARGET_VERSION ?= ""
GARAGE_TARGET_URL ?= "https://example.com/"

SOTA_BOOTLOADER_EXTRA_PARAMS ??= ""
SOTA_BOOTLOADER_BOOTCOMMAND ??= "bootm"
SOTA_BOOTLOADER_KERNEL_ADDR ??= "0x02700000"
SOTA_BOOTLOADER_RAMDISK_ADDR ??= ""
SOTA_BOOTLOADER_FDT_ADDR ??= ""
SOTA_BOOTLOADER_BOOT_PART ??= "mmc 0:1"
SOTA_BOOTLOADER_MAIN_PART ??= "mmc 0:2"
SOTA_BOOTLOADER_ROOT_DEVICE ??= "/dev/mmcblk0p2"

SOTA_MACHINE ??="none"
SOTA_MACHINE_rpi ?= "raspberrypi"
SOTA_MACHINE_porter ?= "porter"
SOTA_MACHINE_m3ulcb = "m3ulcb"
SOTA_MACHINE_intel-corei7-64 ?= "minnowboard"
SOTA_MACHINE_qemux86-64 ?= "qemux86-64"
SOTA_MACHINE_am335x-evm ?= "am335x-evm-wifi"

SOTA_OVERRIDES_BLACKLIST = "ostree ota"
SOTA_REQUIRED_VARIABLES = "OSTREE_REPO OSTREE_BRANCHNAME OSTREE_OSNAME OSTREE_BOOTLOADER OSTREE_BOOT_PARTITION GARAGE_SIGN_REPO GARAGE_TARGET_NAME"

do_image_wic[depends] += " ota-u-boot-script:do_deploy "

inherit sota_sanity sota_${SOTA_MACHINE} image_repo_manifest
