IMAGE_INSTALL_append_sota = " rvi-sota-client"

# live image for OSTree-enabled systems
IMAGE_CLASSES_append_sota = "image_types_ostree image_types_ota"
IMAGE_FSTYPES += "ostreepush otaimg"

# Please redefine OSTREE_REPO in order to have a persistent OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
OSTREE_BRANCHNAME ?= "ota-${MACHINE}"
OSTREE_OSNAME ?= "poky"
OSTREE_INITRAMFS_IMAGE ?= "initramfs-ostree-image"

