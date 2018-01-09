SUMMARY = "Aktualizr configuration with HSM support"
DESCRIPTION = "Systemd service and configurations for Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "aktualizr-native"
RDEPENDS_${PN} = "aktualizr softhsm softhsm-testtoken"

SRC_URI = " \
  file://LICENSE \
  "
PV = "1.0"
PR = "6"


require environment.inc
require credentials.inc

do_install() {
    install -d ${D}${libdir}/sota
    aktualizr_implicit_writer -c ${SOTA_PACKED_CREDENTIALS} --no-root-ca \
        -i ${STAGING_DIR_NATIVE}${libdir}/sota/sota_hsm_test.toml -o ${D}${libdir}/sota/sota.toml -p ${D}
}

FILES_${PN} = " \
                ${libdir}/sota/sota.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
