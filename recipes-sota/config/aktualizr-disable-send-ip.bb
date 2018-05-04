SUMMARY = "Disable IP reporting in Aktualizr"
DESCRIPTION = "Configures aktualizr to disable IP reporting to the server"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

SRC_URI = " \
            file://LICENSE \
            "

do_install_append () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    echo "[telemetry]\nreport_network = false\n" > ${D}${libdir}/sota/conf.d/30-disable-send-ip.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d/30-disable-send-ip.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:

