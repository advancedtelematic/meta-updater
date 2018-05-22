SUMMARY = "Mock smartcard for aktualizr"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"


inherit systemd

RDEPENDS_${PN} = "softhsm libp11"
DEPENDS_append = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' systemd', '', d)}"


SRC_URI = "file://createtoken.service \
	   file://createtoken.sh"

SYSTEMD_SERVICE_${PN} = "createtoken.service"

do_install() {
  install -d ${D}${systemd_unitdir}/system
  install -m 0644 ${WORKDIR}/createtoken.service ${D}${systemd_unitdir}/system/createtoken.service
  install -d ${D}${bindir}
  install -m 0744 ${WORKDIR}/createtoken.sh ${D}${bindir}/createtoken.sh
}

FILES_${PN} = "${bindir}/createtoken.sh \
	       ${systemd_unitdir}/system/createtoken.service"

