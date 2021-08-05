DISTROOVERRIDES .= "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ':sota', '', d)}"

SOTA_CLIENT_PROV ??= "aktualizr-shared-prov"
SOTA_DEPLOY_CREDENTIALS ?= "1"
SOTA_HARDWARE_ID ??= "${MACHINE}"

IMAGE_CLASSES += " image_types_ostree image_types_ota image_repo_manifest"
IMAGE_INSTALL:append:sota = " aktualizr aktualizr-info ${SOTA_CLIENT_PROV} \
                              ostree os-release ostree-kernel ostree-initramfs \
                              ${@'ostree-devicetrees' if oe.types.boolean('${OSTREE_DEPLOY_DEVICETREE}') else ''}"

IMAGE_FSTYPES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'ostreepush garagesign garagecheck ota-ext4 wic', ' ', d)}"
IMAGE_FSTYPES += "${@bb.utils.contains('BUILD_OSTREE_TARBALL', '1', 'ostree.tar.bz2', ' ', d)}"
IMAGE_FSTYPES += "${@bb.utils.contains('BUILD_OTA_TARBALL', '1', 'ota.tar.xz', ' ', d)}"

PACKAGECONFIG:append:pn-curl = " ssl"
PACKAGECONFIG:remove:pn-curl = "gnutls"

WKS_FILE:sota ?= "sdimage-sota.wks"

EXTRA_IMAGEDEPENDS:append:sota = " parted-native mtools-native dosfstools-native"

INITRAMFS_FSTYPES ?= "${@oe.utils.ifelse(d.getVar('OSTREE_BOOTLOADER') == 'u-boot', 'cpio.gz.u-boot', 'cpio.gz')}"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
OSTREE_BRANCHNAME ?= "${SOTA_HARDWARE_ID}"
OSTREE_OSNAME ?= "poky"
OSTREE_BOOTLOADER ??= 'u-boot'
OSTREE_BOOT_PARTITION ??= "/boot"
OSTREE_KERNEL ??= "${KERNEL_IMAGETYPE}"
OSTREE_DEPLOY_DEVICETREE ??= "0"
OSTREE_DEVICETREE ??= "${KERNEL_DEVICETREE}"
OSTREE_MULTI_DEVICETREE_SUPPORT ??= "0"
OSTREE_SYSROOT_READONLY ??= "0"

INITRAMFS_IMAGE ?= "initramfs-ostree-image"

GARAGE_SIGN_REPO ?= "${DEPLOY_DIR_IMAGE}/garage_sign_repo"
GARAGE_SIGN_KEYNAME ?= "garage-key"
GARAGE_TARGET_NAME ?= "${OSTREE_BRANCHNAME}"
GARAGE_TARGET_VERSION ?= ""
GARAGE_TARGET_URL ?= ""
GARAGE_TARGET_EXPIRES ?= ""
GARAGE_TARGET_EXPIRE_AFTER ?= ""
GARAGE_CUSTOMIZE_TARGET ?= ""

SOTA_MACHINE ??="none"
SOTA_MACHINE:rpi ?= "raspberrypi"
SOTA_MACHINE:porter ?= "porter"
SOTA_MACHINE:m3ulcb = "m3ulcb"
SOTA_MACHINE:intel-corei7-64 ?= "minnowboard"
SOTA_MACHINE:qemux86-64 ?= "qemux86-64"
SOTA_MACHINE:am335x-evm ?= "am335x-evm-wifi"
SOTA_MACHINE:freedom-u540 ?= "freedom-u540"

SOTA_OVERRIDES_BLACKLIST = "ostree ota"
SOTA_REQUIRED_VARIABLES = "OSTREE_REPO OSTREE_BRANCHNAME OSTREE_OSNAME OSTREE_BOOTLOADER OSTREE_BOOT_PARTITION GARAGE_SIGN_REPO GARAGE_TARGET_NAME"

inherit sota_sanity sota_${SOTA_MACHINE}
