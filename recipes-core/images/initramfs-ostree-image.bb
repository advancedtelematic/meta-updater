# Netboot initramfs image.
DESCRIPTION = "OSTree initramfs image"

PACKAGE_INSTALL = "ostree-switchroot ostree-initrd busybox base-passwd ${ROOTFS_BOOTSTRAP_INSTALL}"

SYSTEMD_DEFAULT_TARGET = "initrd.target"

# Do not pollute the initrd image with rootfs features
IMAGE_FEATURES = ""

export IMAGE_BASENAME = "initramfs-ostree-image"
IMAGE_LINGUAS = ""

LICENSE = "MIT"

IMAGE_FSTYPES = "ext4.gz"
IMAGE_FSTYPES_append_arm = " ext4.gz.u-boot"
IMAGE_CLASSES_append_arm = " image_types_uboot"

inherit core-image

IMAGE_ROOTFS_SIZE = "8192"

# Users will often ask for extra space in their rootfs by setting this
# globally.  Since this is a initramfs, we don't want to make it bigger
IMAGE_ROOTFS_EXTRA_SPACE = "0"

BAD_RECOMMENDATIONS += "busybox-syslog"


