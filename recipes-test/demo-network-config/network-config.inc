SRC_URI:append = "\
    file://26-${CONF_TYPE}-client.network \
    "

# Because of the dependency on MACHINE.
PACKAGE_ARCH = "${MACHINE_ARCH}"

SECONDARY_INTERFACE ?= "${@ 'eth0' if d.getVar('MACHINE') == 'raspberrypi3' else 'enp0s5'}"

do_install:append() {
    bbnote "Network configuration type to be applied: ${CONF_TYPE}"
    install -d ${D}${libdir}/systemd/network
    install -m 0644 ${WORKDIR}/26-${CONF_TYPE}-client.network ${D}${libdir}/systemd/network/
    sed -i -e 's|@ADDR@|${IP_ADDR}|g' \
           -e 's|@IFNAME@|${SECONDARY_INTERFACE}|g' \
           ${D}${libdir}/systemd/network/26-${CONF_TYPE}-client.network

}

# vim:set ts=4 sw=4 sts=4 expandtab:
