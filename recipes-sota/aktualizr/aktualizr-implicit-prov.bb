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
  file://aktualizr-autoprovision.service \
  file://sota_implicit_prov.toml \
  "

SYSTEMD_SERVICE_${PN} = "aktualizr.service"

inherit systemd

export SOTA_PACKED_CREDENTIALS

do_install() {
    install -d ${D}/${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/aktualizr-autoprovision.service ${D}/${systemd_unitdir}/system/aktualizr.service
    install -d ${D}/usr/lib/sota
    aktualizr_implicit_writer -c ${SOTA_PACKED_CREDENTIALS} \
        -i ${WORKDIR}/sota_implicit_prov.toml -o ${D}/usr/lib/sota/sota.toml -p ${D}
}

FILES_${PN} = " \
                ${systemd_unitdir}/system/aktualizr.service \
                /usr/lib/sota/sota.toml \
                /var/sota/root.crt \
                "
