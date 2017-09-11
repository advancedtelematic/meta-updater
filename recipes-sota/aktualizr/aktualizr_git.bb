SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
DEPENDS = "boost curl openssl jansson libsodium ostree"
RDEPENDS_${PN} = "lshw"

SRC_URI = " \
  git://github.com/advancedtelematic/aktualizr \
  "
SRCREV = "1004efa3f86cef90c012b34620992b5762b741e3"
PV = "1.0+git${SRCPV}"
PR = "6"

S = "${WORKDIR}/git"

inherit cmake systemd

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_OSTREE=ON -DAKTUALIZR_VERSION=${PV}"

FILES_${PN} = " \
                ${bindir}/aktualizr \
                "
