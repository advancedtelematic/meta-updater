SRC_URI_append = "\
    file://26-static-client.network \
    "

SECONDARY_INTERFACE ?= "enp0s5"

do_install_append() {
    install -d ${D}/usr/lib/systemd/network
    install -m 0644 ${WORKDIR}/26-static-client.network ${D}/usr/lib/systemd/network/
    sed -i -e 's|@ADDR@|${IP_ADDR}|g' \
           -e 's|@IFNAME@|${SECONDARY_INTERFACE}|g' \
           ${D}/usr/lib/systemd/network/26-static-client.network

}

# vim:set ts=4 sw=4 sts=4 expandtab:
