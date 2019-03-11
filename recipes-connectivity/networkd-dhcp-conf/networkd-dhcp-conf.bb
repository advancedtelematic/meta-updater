SUMMARY = "systemd-networkd config to setup wired interface with dhcp"
DESCRIPTION = "Provides automatic dhcp network configuration for wired \
interfaces through systemd-networkd"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch systemd

RPROVIDES_${PN} = "virtual/network-configuration"

SRC_URI_append = " file://20-wired-dhcp.network"
PR = "r1"

RDEPENDS_${PN} = "systemd"

S = "${WORKDIR}"

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} = "${systemd_unitdir}/network/*"

DEV_MATCH_DIRECTIVE ?= "Name=en*"

do_install() {
    install -d ${D}/${systemd_unitdir}/network
    install -m 0644 ${WORKDIR}/20-wired-dhcp.network ${D}/${systemd_unitdir}/network
    sed -i -e 's|@MATCH_DIRECTIVE@|${DEV_MATCH_DIRECTIVE}|g' ${D}${systemd_unitdir}/network/20-wired-dhcp.network
}
