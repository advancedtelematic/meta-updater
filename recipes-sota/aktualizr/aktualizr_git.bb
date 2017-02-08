SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"

LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

inherit cmake systemd

S = "${WORKDIR}/git"

SRCREV = "ca1f1fa1e8e02b87696a0b35fdaa8bd14b05d2a3"

SRC_URI = " \
	git://github.com/advancedtelematic/aktualizr \
	"

DEPENDS = "boost curl openssl"
RDEPENDS = ""

EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Release"

do_install() {
  install -d ${D}${bindir}
  install -m 0755 ${WORKDIR}/build/target/aktualizr ${D}${bindir}/aktualizr
}

FILES_${PN} = " \
                ${bindir}/aktualizr \
		"
