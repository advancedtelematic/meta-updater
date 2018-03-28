DESCRIPTION = "Sample network configuration for an Uptane Primary"
LICENSE = "CLOSED"

inherit allarch

SRC_URI = "file://25-dhcp-server.network \
           file://systemd-networkd-wait-online.service.override \
           "


FILES_${PN} = "/usr/lib/systemd/network \
               /usr/lib/systemd/system/systemd-networkd-wait-online.service.d \
               "

PR = "1"

do_install() {
    install -d ${D}/usr/lib/systemd/network
    install -m 0644 ${WORKDIR}/25-dhcp-server.network ${D}/usr/lib/systemd/network/
    install -d ${D}/usr/lib/systemd/system/systemd-networkd-wait-online.service.d
    install -m 0644 ${WORKDIR}/systemd-networkd-wait-online.service.override ${D}/usr/lib/systemd/system/systemd-networkd-wait-online.service.d/override.conf
}
