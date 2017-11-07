FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI_append = " \
           file://fix-posix-acl.patch \
           "
