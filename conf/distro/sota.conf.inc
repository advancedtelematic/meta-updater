# Common settings to enable SOTA
# This can be pulled into a custom distribution, or included in
# local.conf directly with:
#
# require conf/distro/sota.conf.inc

DISTRO_FEATURES:append = " sota usrmerge"
DISTRO_FEATURES_NATIVE:append = " sota"
INHERIT += " sota"

# Reproducible builds are enabled by default since kirkstone
# Use 0 as mtime, the same as OSTree is using.
export SOURCE_DATE_EPOCH = "0"
REPRODUCIBLE_TIMESTAMP_ROOTFS = "0"

HOSTTOOLS += "git sync sha256sum"
HOSTTOOLS_NONFATAL += "java repo"
