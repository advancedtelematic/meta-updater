SUMMARY = "systemd-networkd config to setup wired interface with dhcp"
DESCRIPTION = "Provides automatic dhcp network configuration for wired \
interfaces through systemd-networkd"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit systemd features_check

RPROVIDES:${PN} = "network-configuration"

SRC_URI = " \
  file://20-wired-dhcp.network \
  file://resolvconf-clean \
  file://clean-connman-symlink.service \
  "
PR = "r1"

REQUIRED_DISTRO_FEATURES = "systemd"
RCONFLICTS:${PN} = "connman"

S = "${WORKDIR}"

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} = " \
        ${systemd_unitdir}/network/* \
        ${sbindir}/resolvconf-clean \
        ${systemd_unitdir}/system/clean-connman-symlink.service \
        "

SYSTEMD_SERVICE:${PN} = "clean-connman-symlink.service"

DEV_MATCH_DIRECTIVE ?= "Name=en*"

do_install() {
    install -d ${D}/${systemd_unitdir}/network
    install -m 0644 ${WORKDIR}/20-wired-dhcp.network ${D}${systemd_unitdir}/network
    sed -i -e 's|@MATCH_DIRECTIVE@|${DEV_MATCH_DIRECTIVE}|g' ${D}${systemd_unitdir}/network/20-wired-dhcp.network

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/resolvconf-clean ${D}${sbindir}/resolvconf-clean
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/clean-connman-symlink.service ${D}${systemd_unitdir}/system/clean-connman-symlink.service
}
