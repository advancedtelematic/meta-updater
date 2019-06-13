CONF_TYPE ?= "${@ 'multihomed' if d.getVar('MACHINE') == 'raspberrypi3' and d.getVar('IMAGE_TYPE') == 'primary' else 'static'}"

SRC_URI_append = "\
    file://26-${CONF_TYPE}-client.network \
    "

SECONDARY_INTERFACE ?= "${@ 'eth0' if d.getVar('MACHINE') == 'raspberrypi3' else 'enp0s5'}"

do_install_append() {
    bbnote "Network configuration type to be applied: ${CONF_TYPE}"
    install -d ${D}/usr/lib/systemd/network
    install -m 0644 ${WORKDIR}/26-${CONF_TYPE}-client.network ${D}/usr/lib/systemd/network/
    sed -i -e 's|@ADDR@|${IP_ADDR}|g' \
           -e 's|@IFNAME@|${SECONDARY_INTERFACE}|g' \
           ${D}/usr/lib/systemd/network/26-${CONF_TYPE}-client.network

}

# vim:set ts=4 sw=4 sts=4 expandtab:
