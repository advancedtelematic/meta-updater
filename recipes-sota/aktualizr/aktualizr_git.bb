SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"

LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

inherit cmake systemd

S = "${WORKDIR}/git"

SRCREV = "f2275e9938f5c942c9e51a3966b1ad91acd65367"

SRC_URI = "git://github.com/advancedtelematic/aktualizr"

DEPENDS = "boost curl openssl jansson"
RDEPENDS = ""

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTS=OFF"

FILES_${PN} = " \
                ${bindir}/aktualizr \
		"
