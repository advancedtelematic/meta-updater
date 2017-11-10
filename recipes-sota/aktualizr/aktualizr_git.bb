SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "boost curl openssl libarchive libsodium "
DEPENDS_append_class-target = "jansson ostree ${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', ' libp11', '', d)} "
DEPENDS_append_class-native = "glib-2.0-native "

RDEPENDS_${PN}_class-target = "lshw "
RDEPENDS_${PN}_append_class-target = "${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', ' engine-pkcs11', '', d)} "
RDEPENDS_${PN}_append_class-target = "${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm-test', ' softhsm softhsm-testtoken', '', d)} "

PV = "1.0+git${SRCPV}"
PR = "7"

SRC_URI = " \
  git://github.com/advancedtelematic/aktualizr;branch=${BRANCH} \
  "
SRCREV = "f043191ae622a96cf2f4d48f9073d5cfa9f16e3f"
BRANCH ?= "master"

S = "${WORKDIR}/git"

inherit cmake

BBCLASSEXTEND =+ "native"

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DAKTUALIZR_VERSION=${PV} "
EXTRA_OECMAKE_append_class-target = "-DBUILD_OSTREE=ON ${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', '-DBUILD_P11=ON', '', d)} "
EXTRA_OECMAKE_append_class-native = "-DBUILD_SOTA_TOOLS=ON -DBUILD_OSTREE=OFF "

do_install_append () {
    rm -f ${D}${bindir}/aktualizr_cert_provider
}
do_install_append_class-target () {
    rm -f ${D}${bindir}/aktualizr_implicit_writer
}
do_install_append_class-native () {
    rm -f ${D}${bindir}/aktualizr
}

FILES_${PN}_class-target = " \
                ${bindir}/aktualizr \
                "
FILES_${PN}_class-native = " \
                ${bindir}/aktualizr_implicit_writer \
                ${bindir}/garage-push \
                "
