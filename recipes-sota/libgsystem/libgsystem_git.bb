SUMMARY = "GIO-based library, targeted primarily for use by operating system components"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=5f30f0716dfdd0d91eb439ebec522ec2"

SRC_URI = "gitsm://github.com/GNOME/libgsystem.git"
SRCREV="d606bec68ddfea78de4b03c3f3568afb71bdc1ce"

S = "${WORKDIR}/git"

DEPENDS += "attr glib-2.0 pkgconfig libcap"

RDEPENDS_${PN} = "systemd liblzma"

inherit autotools-brokensep

BBCLASSEXTEND += "native"

do_configure() {
 NOCONFIGURE=true ./autogen.sh
 oe_runconf
}
