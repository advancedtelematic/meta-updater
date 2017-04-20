DISTRO_FEATURES_append = " sota"
OVERRIDES .= ":sota"

IMAGE_INSTALL_append = " ostree os-release"

# live image for OSTree-enabled systems
IMAGE_CLASSES += "image_types_ostree image_types_ota"
IMAGE_FSTYPES += "ostreepush otaimg"

# if don't build wic image unless IMAGE_BOOT_FILES is set. Prevents build from failing
#   on machines that don't support updater yet
IMAGE_FSTYPES += "${@' wic' if (d.getVar("IMAGE_BOOT_FILES", True)) else ''}"
WKS_FILE ?= "sdimage-sota.wks"
do_image_wic[depends] += "${IMAGE_BASENAME}:do_image_otaimg"

EXTRA_IMAGEDEPENDS += " parted-native mtools-native dosfstools-native"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
OSTREE_BRANCHNAME ?= "ota-${MACHINE}"
OSTREE_OSNAME ?= "poky"
OSTREE_INITRAMFS_IMAGE ?= "initramfs-ostree-image"

