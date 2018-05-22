SUMMARY = "Smartcard HSM driver"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=55b854a477953696452f698a3af5de1c"

inherit autotools-brokensep


SRC_URI = "git://github.com/CardContact/sc-hsm-embedded.git;branch=master"
SRCREV="a45155d4249575ebdfb16ff26fdedbc4c4813002"

S = "${WORKDIR}/git"

DEPENDS += " openssl pcsc-lite"

do_configure() {
 autoreconf -fi
 oe_runconf
}

FILES_${PN} += "${libdir}"
FILES_SOLIBSDEV = ""

