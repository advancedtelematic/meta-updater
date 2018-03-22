DESCRIPTION = "Sample network configuration for an Uptane Secondary"
LICENSE = "CLOSED"

inherit allarch

SRC_URI = "file://26-dhcp-client.network"


FILES_${PN} = "/usr/lib/systemd/network"

PR = "1"

do_install() {
    install -d ${D}/usr/lib/systemd/network
    install -m 0644 ${WORKDIR}/26-dhcp-client.network ${D}/usr/lib/systemd/network/
}
