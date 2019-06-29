python __anonymous() {
    if bb.utils.contains('DISTRO_FEATURES', 'sota', True, False, d):
        d.appendVarFlag("do_image_wic", "depends", " %s:do_image_otaimg" % d.getVar("IMAGE_BASENAME", True))
}

DISTROOVERRIDES .= "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ':sota', '', d)}"

HOSTTOOLS_NONFATAL += "java"

SOTA_CLIENT ??= "aktualizr"
SOTA_CLIENT_PROV ??= "aktualizr-shared-prov"
SOTA_DEPLOY_CREDENTIALS ?= "1"
SOTA_HARDWARE_ID ??= "${MACHINE}"

# Translate old provisioning recipe names into the new versions.
python () {
    prov = d.getVar("SOTA_CLIENT_PROV")
    if prov == "aktualizr-auto-prov":
        bb.warn('aktualizr-auto-prov is deprecated. Please use aktualizr-shared-prov instead.')
        d.setVar("SOTA_CLIENT_PROV", "aktualizr-shared-prov")
    elif prov == "aktualizr-ca-implicit-prov":
        bb.warn('aktualizr-ca-implicit-prov is deprecated. Please use aktualizr-device-prov instead.')
        d.setVar("SOTA_CLIENT_PROV", "aktualizr-device-prov")
    elif prov == "aktualizr-hsm-prov":
        bb.warn('aktualizr-hsm-prov is deprecated. Please use aktualizr-device-prov-hsm instead.')
        d.setVar("SOTA_CLIENT_PROV", "aktualizr-device-prov-hsm")
}

IMAGE_INSTALL_append_sota = " ostree os-release ${SOTA_CLIENT} ${SOTA_CLIENT_PROV}"
IMAGE_CLASSES += " image_types_ostree image_types_ota"
IMAGE_FSTYPES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'ostreepush garagesign garagecheck otaimg wic', ' ', d)}"

PACKAGECONFIG_append_pn-curl = " ssl"
PACKAGECONFIG_remove_pn-curl = "gnutls"

WKS_FILE_sota ?= "sdimage-sota.wks"

EXTRA_IMAGEDEPENDS_append_sota = " parted-native mtools-native dosfstools-native"

OSTREE_INITRAMFS_FSTYPES ??= "${@oe.utils.ifelse(d.getVar('OSTREE_BOOTLOADER', True) == 'u-boot', 'ext4.gz.u-boot', 'ext4.gz')}"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
export OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
export OSTREE_BRANCHNAME ?= "${SOTA_HARDWARE_ID}"
export OSTREE_OSNAME ?= "poky"
export OSTREE_BOOTLOADER ??= 'u-boot'
export OSTREE_BOOT_PARTITION ??= "/boot"

OSTREE_INITRAMFS_IMAGE ?= "initramfs-ostree-image"

GARAGE_SIGN_REPO ?= "${DEPLOY_DIR_IMAGE}/garage_sign_repo"
GARAGE_SIGN_KEYNAME ?= "garage-key"
GARAGE_TARGET_NAME ?= "${OSTREE_BRANCHNAME}"
GARAGE_TARGET_VERSION ?= ""
GARAGE_TARGET_URL ?= ""

SOTA_MACHINE ??="none"
SOTA_MACHINE_rpi ?= "raspberrypi"
SOTA_MACHINE_porter ?= "porter"
SOTA_MACHINE_m3ulcb = "m3ulcb"
SOTA_MACHINE_h3ulcb = "h3ulcb"
SOTA_MACHINE_intel-corei7-64 ?= "minnowboard"
SOTA_MACHINE_qemux86-64 ?= "qemux86-64"
SOTA_MACHINE_am335x-evm ?= "am335x-evm"

inherit sota_${SOTA_MACHINE}

inherit image_repo_manifest
