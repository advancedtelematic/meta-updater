SUMMARY = "Aktualizr systemd service and configurations"
DESCRIPTION = "Systemd service and configurations for Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
RDEPENDS_${PN} = "aktualizr"

SRC_URI = " \
  file://LICENSE \
  file://aktualizr-manual-provision.service \
  file://aktualizr-autoprovision.service \
  file://sota_autoprov.toml \
  "
PV = "1.0"
PR = "6"

SYSTEMD_SERVICE_${PN} = "aktualizr.service"

inherit systemd

export SOTA_PACKED_CREDENTIALS

do_install_append() {
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
      install -d ${D}/${systemd_unitdir}/system
      install -m 0644 ${WORKDIR}/aktualizr-autoprovision.service ${D}/${systemd_unitdir}/system/aktualizr.service
      install -d ${D}/usr/lib/sota
      install -m "0644" ${WORKDIR}/sota_autoprov.toml ${D}/usr/lib/sota/sota.toml
    else
      install -d ${D}/${systemd_unitdir}/system
      install -m 0644 ${WORKDIR}/aktualizr-manual-provision.service ${D}/${systemd_unitdir}/system/aktualizr.service
    fi
}

FILES_${PN} = " \
                ${systemd_unitdir}/system/aktualizr.service \
                /usr/lib/sota/sota.toml \
                "
