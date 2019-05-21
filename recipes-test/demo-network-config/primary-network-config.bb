DESCRIPTION = "Sample network configuration for an Uptane Primary"
LICENSE = "CLOSED"

inherit allarch

SRC_URI = "\
    file://27-dhcp-client-external.network \
    "

FILES_${PN} = "/usr/lib/systemd/network"

PR = "1"

do_install() {
    install -d ${D}/usr/lib/systemd/network
    install -m 0644 ${WORKDIR}/27-dhcp-client-external.network ${D}/usr/lib/systemd/network/
}

PRIMARY_IP ?= "10.0.3.1"
IP_ADDR = "${PRIMARY_IP}"

require static-network-config.inc

# vim:set ts=4 sw=4 sts=4 expandtab:
