python __anonymous() {
    if bb.utils.contains('DISTRO_FEATURES', 'sota', True, False, d):
        d.appendVarFlag("do_image_wic", "depends", " %s:do_image_otaimg" % d.getVar("IMAGE_BASENAME", True))
}

OVERRIDES .= "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ':sota', '', d)}"

HOSTTOOLS_NONFATAL += "java"

SOTA_CLIENT ??= "aktualizr"
SOTA_CLIENT_PROV ??= "aktualizr-auto-prov"
IMAGE_INSTALL_append_sota = " ostree os-release ${SOTA_CLIENT} ${SOTA_CLIENT_PROV}"
IMAGE_CLASSES += " image_types_ostree image_types_ota"
IMAGE_FSTYPES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'ostreepush garagesign garagecheck otaimg wic', ' ', d)}"

PACKAGECONFIG_append_pn-curl = " ssl"
PACKAGECONFIG_remove_pn-curl = "gnutls"

WKS_FILE_sota ?= "sdimage-sota.wks"

EXTRA_IMAGEDEPENDS_append_sota = " parted-native mtools-native dosfstools-native"

OSTREE_INITRAMFS_FSTYPES ??= "${@oe.utils.ifelse(d.getVar('OSTREE_BOOTLOADER', True) == 'u-boot', 'ext4.gz.u-boot', 'ext4.gz')}"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
OSTREE_BRANCHNAME ?= "${MACHINE}"
OSTREE_OSNAME ?= "poky"
OSTREE_INITRAMFS_IMAGE ?= "initramfs-ostree-image"
OSTREE_BOOTLOADER ??= 'u-boot'

GARAGE_SIGN_REPO ?= "${DEPLOY_DIR_IMAGE}/garage_sign_repo"
GARAGE_SIGN_KEYNAME ?= "garage-key"
GARAGE_TARGET_NAME ?= "${OSTREE_BRANCHNAME}"
GARAGE_TARGET_VERSION ?= ""
GARAGE_TARGET_URL ?= "https://example.com/"

SOTA_MACHINE ??="none"
SOTA_MACHINE_rpi ?= "raspberrypi"
SOTA_MACHINE_porter ?= "porter"
SOTA_MACHINE_m3ulcb = "m3ulcb"
SOTA_MACHINE_h3ulcb = "h3ulcb"
SOTA_MACHINE_intel-corei7-64 ?= "minnowboard"
SOTA_MACHINE_qemux86-64 ?= "qemux86-64"
SOTA_MACHINE_am335x-evm ?= "am335x-evm-wifi"

inherit sota_${SOTA_MACHINE}

inherit image_repo_manifest
