SUMMARY = "Mock smartcard for aktualizr"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit systemd

RDEPENDS:${PN} = "can-utils"

SRC_URI = "file://slcand@.service"

SYSTEMD_SERVICE:${PN} = "slcand@.service"

do_install() {
  install -d ${D}${systemd_unitdir}/system
  install -m 0644 ${WORKDIR}/slcand@.service ${D}${systemd_unitdir}/system/slcand@.service
}

FILES:${PN} = "${systemd_unitdir}/system/createtoken.service"

