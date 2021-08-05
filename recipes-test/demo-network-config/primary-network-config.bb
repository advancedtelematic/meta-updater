DESCRIPTION = "Sample network configuration for an Uptane Primary"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

SRC_URI = "\
    file://27-dhcp-client-external.network \
    "

FILES:${PN} = "${libdir}/systemd/network"

PR = "1"

do_install() {
    install -d ${D}${libdir}/systemd/network
    install -m 0644 ${WORKDIR}/27-dhcp-client-external.network ${D}${libdir}/systemd/network/
}

PRIMARY_IP ?= "192.168.254.1"

IP_ADDR = "${PRIMARY_IP}"
CONF_TYPE ?= "${@ 'multihomed' if d.getVar('MACHINE') == 'raspberrypi3' and d.getVar('RPI_WIFI_ENABLE') != '1' else 'static'}"

require network-config.inc

# vim:set ts=4 sw=4 sts=4 expandtab:
