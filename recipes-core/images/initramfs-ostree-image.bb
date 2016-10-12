# Netboot initramfs image.
DESCRIPTION = "OSTree initramfs image"

PACKAGE_INSTALL = "ostree-switchroot ostree-initrd busybox base-passwd ${ROOTFS_BOOTSTRAP_INSTALL}"

SYSTEMD_DEFAULT_TARGET = "initrd.target"

# Do not pollute the initrd image with rootfs features
IMAGE_FEATURES = ""

export IMAGE_BASENAME = "initramfs-ostree-image"
IMAGE_LINGUAS = ""

LICENSE = "MIT"

# was ${INITRAMFS_FSTYPES} which defaults to cpio.gz
# due to xattr, we need ext3/4
IMAGE_FSTYPES = "ext4.gz"
inherit core-image

IMAGE_ROOTFS_SIZE = "8192"

BAD_RECOMMENDATIONS += "busybox-syslog"


