python __anonymous() {
    if bb.utils.contains('DISTRO_FEATURES', 'sota', True, False, d):
        d.appendVarFlag("do_image_wic", "depends", " %s:do_image_otaimg" % d.getVar("IMAGE_BASENAME", True))
}

OVERRIDES .= "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ':sota', '', d)}"

SOTA_CLIENT ??= "aktualizr"
IMAGE_INSTALL_append_sota = " ostree os-release ${SOTA_CLIENT}"
IMAGE_CLASSES += " image_types_ostree"
IMAGE_FSTYPES += "${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'ostreepush', ' ', d)}"

WKS_FILE_sota ?= "sdimage-sota.wks"

EXTRA_IMAGEDEPENDS_append_sota = " parted-native mtools-native dosfstools-native"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
# For UPTANE operation, OSTREE_BRANCHNAME must start with "${MACHINE}-"
OSTREE_BRANCHNAME ?= "${MACHINE}-ota"
OSTREE_OSNAME ?= "poky"
OSTREE_INITRAMFS_IMAGE ?= "initramfs-ostree-image"

SOTA_MACHINE ??="none"
SOTA_MACHINE_raspberrypi2 ?= "raspberrypi"
SOTA_MACHINE_raspberrypi3 ?= "raspberrypi"
SOTA_MACHINE_porter ?= "porter"
SOTA_MACHINE_m3ulcb = "m3ulcb"
SOTA_MACHINE_intel-corei7-64 ?= "minnowboard"
SOTA_MACHINE_qemux86-64 ?= "qemux86-64"
SOTA_MACHINE_am335x-evm ?= "am335x-evm-wifi"

inherit sota_${SOTA_MACHINE}
