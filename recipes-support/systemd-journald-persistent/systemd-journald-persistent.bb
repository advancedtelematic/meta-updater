SUMMARY = "Configuration for systemd-journald"
DESCRIPTION = "Provides configuration for systemd-journald, so that logs are \
stored on persistent storage"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI:append = " file://10-persistent-journal.conf"
PR = "r1"

S = "${WORKDIR}"

FILES:${PN} = "${systemd_unitdir}/journald.conf.d/*"

do_install() {
    install -d ${D}/${systemd_unitdir}/journald.conf.d
    install -m 0644 ${WORKDIR}/10-persistent-journal.conf ${D}/${systemd_unitdir}/journald.conf.d
}

