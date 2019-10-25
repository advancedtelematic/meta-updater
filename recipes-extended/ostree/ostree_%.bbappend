FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
    file://0001-Makefile-declare-ostree_boot_SCRIPTS-and-append-valu.patch \
"

PACKAGECONFIG_append = " curl libarchive static"
PACKAGECONFIG_class-native_append = " curl"
PACKAGECONFIG_remove = "soup"
PACKAGECONFIG_class-native_remove = "soup"

EXTRA_OECONF += " \
    --with-builtin-grub2-mkconfig \
"

FILES_${PN} += " \
    ${libdir}/ostree/ostree-grub-generator \
"
