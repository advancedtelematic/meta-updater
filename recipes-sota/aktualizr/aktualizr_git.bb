SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"

LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

inherit cmake systemd

S = "${WORKDIR}/git"
PV = "1.0+git${SRCPV}"

SRCREV = "4e9344ae375a444f02b964dca52fe808010d17df"

SRC_URI = "git://github.com/advancedtelematic/aktualizr"

DEPENDS = "boost curl openssl jansson libsodium ostree"
RDEPENDS = ""

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTS=OFF -DBUILD_OSTREE=ON"

FILES_${PN} = " \
                ${bindir}/aktualizr \
		"
