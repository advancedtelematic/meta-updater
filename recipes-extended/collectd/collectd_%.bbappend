do_install:append() {
    printf "<Include \"${sysconfdir}/collectd.conf.d\">\nFilter \"*.conf\"\n</Include>\n" >> ${D}/${sysconfdir}/collectd.conf

    install -d ${D}/${sysconfdir}/collectd.conf.d
}
