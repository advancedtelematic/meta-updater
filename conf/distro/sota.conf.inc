# Common settings to enable SOTA
# This can be pulled into a custom distribution, or included in
# local.conf directly with:
#
# require conf/distro/sota.conf.inc

DISTRO_FEATURES_append = " sota usrmerge"
DISTRO_FEATURES_NATIVE_append = " sota"
INHERIT += " sota"
# Prelinking increases the size of downloads and causes build errors
USER_CLASSES_remove = "image-prelink"

# Enable reproducible builds. Use 0 as mtime, the same as OSTree is using.
INHERIT_remove = "reproducible_build"
INHERIT += "reproducible_build_simple"

export SOURCE_DATE_EPOCH = "0"
REPRODUCIBLE_TIMESTAMP_ROOTFS = "0"

HOSTTOOLS += "git sync sha256sum"
HOSTTOOLS_NONFATAL += "java repo python"
