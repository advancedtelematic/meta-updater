SUMMARY = "Library for using PKCS"
DESCRIPTION = "\
Libp11 is a library implementing a small layer on top of PKCS \
make using PKCS"
HOMEPAGE = "http://www.opensc-project.org/libp11"
SECTION = "Development/Libraries"
LICENSE = "LGPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=fad9b3332be894bab9bc501572864b29"
DEPENDS = "libtool openssl"
RDEPENDS_${PN} += " opensc"

SRC_URI = "git://github.com/OpenSC/libp11.git"
SRCREV = "57ca68ff67efa08e3be1f26dec6d23bf5bb977f2"

PV = "0.4.9+git${SRCPV}"

S = "${WORKDIR}/git"

inherit autotools pkgconfig

# Currently, Makefile dependencies are incorrectly defined which causes build errors
# if the number of jobs is high
# See https://github.com/OpenSC/libp11/issues/94
PARALLEL_MAKE = ""
EXTRA_OECONF = "--disable-static"

do_install_append () {
    rm -rf ${D}${libdir}/*.la
    rm -rf ${D}${docdir}/${BPN}
}

FILES_${PN} = "${libdir}/engines*/pkcs11.so \
               ${libdir}/engines*/libpkcs11${SOLIBS} \
               ${libdir}/libp11${SOLIBS}"

FILES_${PN}-dev = " \
                   ${libdir}/engines*/libpkcs11${SOLIBSDEV} \
                   ${libdir}/libp11${SOLIBSDEV} \
                   ${libdir}/pkgconfig/libp11.pc \
                   /usr/include"
