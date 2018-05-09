SUMMARY = "Aktualizr example interface"
DESCRIPTION = "Aktualizr example interface for legacy secondaries"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

SRC_URI = " \
            file://LICENSE \
            file://30-example-interface.toml \
            "

do_install_append () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/30-example-interface.toml ${D}${libdir}/sota/conf.d/30-example-interface.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d/30-example-interface.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
