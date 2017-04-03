DISTRO_FEATURES_append = " sota"
OVERRIDES .= ":sota"

IMAGE_INSTALL_append = " ostree"

# live image for OSTree-enabled systems
IMAGE_CLASSES += "image_types_ostree image_types_ota"
IMAGE_FSTYPES += "ostreepush otaimg wic"
WKS_FILE = "sdimage-sota.wks"
do_image_wic[depends] += "${IMAGE_BASENAME}:do_image_otaimg"

# No working WKS for Raspberry Pi yet
IMAGE_FSTYPES_remove_raspberrypi3 = "wic"
IMAGE_FSTYPES_remove_raspberrypi2 = "wic"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
OSTREE_BRANCHNAME ?= "ota-${MACHINE}"
OSTREE_OSNAME ?= "poky"
OSTREE_INITRAMFS_IMAGE ?= "initramfs-ostree-image"

