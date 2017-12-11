SUMMARY = "Aktualizr systemd service and configurations"
DESCRIPTION = "Systemd service and configurations for implicitly provisioning Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
DEPENDS = "aktualizr-native"
RDEPENDS_${PN} = "aktualizr"
PV = "1.0"
PR = "1"

SRC_URI = " \
  file://LICENSE \
  file://aktualizr.service \
  "

SYSTEMD_SERVICE_${PN} = "aktualizr.service"

inherit systemd

require environment.inc
require credentials.inc

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/aktualizr.service ${D}${systemd_unitdir}/system/aktualizr.service
    install -d ${D}${libdir}/sota
    aktualizr_implicit_writer -c ${SOTA_PACKED_CREDENTIALS} \
        -i ${STAGING_DIR_NATIVE}${libdir}/sota/sota_implicit_prov.toml -o ${D}${libdir}/sota/sota.toml -p ${D}
}

FILES_${PN} = " \
                ${systemd_unitdir}/system/aktualizr.service \
                ${libdir}/sota/sota.toml \
                ${libdir}/sota/root.crt \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
