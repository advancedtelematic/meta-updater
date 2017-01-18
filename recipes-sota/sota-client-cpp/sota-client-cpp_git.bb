SUMMARY = "SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/sota_client_cpp"
SECTION = "base"

LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

inherit cmake systemd

S = "${WORKDIR}/git"

SRCREV = "a974dc2eea47594a6177f4c69c2d937a819aa7b3"

SRC_URI = " \
	git://github.com/advancedtelematic/sota_client_cpp \
	"

DEPENDS = "boost curl openssl"
RDEPENDS = ""

EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Release"

do_install() {
  install -d ${D}${bindir}
  install -m 0755 ${WORKDIR}/build/target/sota_client ${D}${bindir}/sota_client_cpp
}

FILES_${PN} = " \
                ${bindir}/sota_client_cpp \
		"
