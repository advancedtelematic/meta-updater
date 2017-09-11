
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "file://Disable-hard-link-check-by-default.patch "
export SPOOL_DIR = "${datadir}/cronie-spool"
FILES_${PN} += "${datadir}/cronie-spool"

do_install_append () {
    install -d ${D}${datadir}/cronie-spool/
}
