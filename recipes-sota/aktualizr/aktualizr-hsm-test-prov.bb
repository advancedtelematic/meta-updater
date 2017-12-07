SUMMARY = "Aktualizr systemd service and configuration with HSM support"
DESCRIPTION = "Systemd service and configurations for Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "aktualizr-native"
RDEPENDS_${PN} = "aktualizr softhsm softhsm-testtoken"

SRC_URI = " \
  file://LICENSE \
  file://aktualizr.service \
  "
PV = "1.0"
PR = "6"

SYSTEMD_SERVICE_${PN} = "aktualizr.service"

inherit systemd

require environment.inc

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/aktualizr.service ${D}${systemd_unitdir}/system/aktualizr.service
    install -d ${D}${libdir}/sota
    aktualizr_implicit_writer -c ${SOTA_PACKED_CREDENTIALS} --no-root-ca \
        -i ${STAGING_DIR_NATIVE}${libdir}/sota/sota_hsm_test.toml -o ${D}${libdir}/sota/sota.toml -p ${D}
}

FILES_${PN} = " \
                ${systemd_unitdir}/system/aktualizr.service \
                ${libdir}/sota/sota.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
