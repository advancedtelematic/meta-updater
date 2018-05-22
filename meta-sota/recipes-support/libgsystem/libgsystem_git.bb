SUMMARY = "GIO-based library, targeted primarily for use by operating system components"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=5f30f0716dfdd0d91eb439ebec522ec2"

SRC_URI = "gitsm://github.com/GNOME/libgsystem.git"
SRCREV="d606bec68ddfea78de4b03c3f3568afb71bdc1ce"

S = "${WORKDIR}/git"

inherit autotools-brokensep gobject-introspection

DEPENDS += "attr glib-2.0 pkgconfig libcap gtk-doc-native gpgme"
RDEPENDS_${PN} = "xz "
RDEPENDS_${PN}_append = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' systemd', '', d)}"

RDEPENDS_${PN}_remove_class-native = "systemd-native"

BBCLASSEXTEND = "native"

export STAGING_INCDIR
export STAGING_LIBDIR

do_configure() {
 #NOCONFIGURE=true ./autogen.sh
 autoreconf -vfi
 oe_runconf
}

do_compile_prepend() {
 export BUILD_SYS="${BUILD_SYS}"
 export HOST_SYS="${HOST_SYS}"
}

FILES_${PN} += " \
    ${datadir} \
    ${datadir}/gir-1.0 \
    ${datadir}/gir-1.0/GSystem-1.0.gir \
    ${libdir}/girepository-1.0/ \
    ${libdir}/girepository-1.0/GSystem-1.0.typelib \
"
