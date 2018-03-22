DESCRIPTION = "Sample network configuration for an Uptane Primary"
LICENSE = "CLOSED"

inherit allarch

SRC_URI = "file://25-dhcp-server.network"


FILES_${PN} = "/usr/lib/systemd/network"

PR = "1"

do_install() {
    install -d ${D}/usr/lib/systemd/network
    install -m 0644 ${WORKDIR}/25-dhcp-server.network ${D}/usr/lib/systemd/network/
}
