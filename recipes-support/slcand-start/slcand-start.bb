SUMMARY = "Mock smartcard for aktualizr"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"


inherit systemd

RDEPENDS_${PN} = "can-utils"

SRC_URI = "file://slcand@.service"

SYSTEMD_SERVICE_${PN} = "slcand@.service"

do_install() {
  install -d ${D}${systemd_unitdir}/system
  install -m 0644 ${WORKDIR}/slcand@.service ${D}${systemd_unitdir}/system/slcand@.service
}

FILES_${PN} = "${systemd_unitdir}/system/createtoken.service"

