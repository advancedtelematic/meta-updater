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

inherit core-image

IMAGE_ROOTFS_SIZE = "8192"

BAD_RECOMMENDATIONS += "busybox-syslog"


