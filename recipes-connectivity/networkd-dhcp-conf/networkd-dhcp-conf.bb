SUMMARY = "systemd-networkd config to setup wired interface with dhcp"
DESCRIPTION = "Provides automatic dhcp network configuration for wired \
interfaces through systemd-networkd"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit systemd

SRC_URI_append = " file://20-wired-dhcp.network"
PR = "r1"

RDEPENDS_${PN} = "systemd"

S = "${WORKDIR}"

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} = "${systemd_unitdir}/network/*"

do_install() {
    install -d ${D}/${systemd_unitdir}/network
    install -m 0644 ${WORKDIR}/20-wired-dhcp.network ${D}/${systemd_unitdir}/network
}
