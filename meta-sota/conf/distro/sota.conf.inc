# Common settings to enable SOTA
# This can be pulled into a custom distribution, or included in
# local.conf directly with:
#
# require conf/distro/sota.conf.inc

DISTRO_FEATURES_append = " sota"
DISTRO_FEATURES_NATIVE_append = " sota"
INHERIT += " sota"
# Prelinking increases the size of downloads and causes build errors
USER_CLASSES_remove = "image-prelink"

HOSTTOOLS_append = " sync sha256sum"
