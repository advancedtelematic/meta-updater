SUMMARY = "HSM emulator"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ef3f77a3507c3d91e75b9f2bdaee4210"

inherit autotools-brokensep


SRC_URI = "git://github.com/opendnssec/SoftHSMv2.git;branch=master \
	   file://0001-Cross-compilation-tweaks.patch"
SRCREV="1f7498c0c65b1b1ad5e1bdbd87e9d4b100705745"

S = "${WORKDIR}/git"

DEPENDS += " openssl"

EXTRA_OECONF = "--disable-gost --with-openssl=${STAGING_LIBDIR}/.."

do_configure() {
 unset docdir
 sh ./autogen.sh
 oe_runconf
}

FILES_${PN} = "${bindir} \
	       ${libdir}/softhsm \
	       ${sysconfdir} \
	       ${localstatedir}/lib/softhsm "
