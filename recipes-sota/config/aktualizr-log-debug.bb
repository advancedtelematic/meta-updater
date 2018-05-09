SUMMARY = "Set debug logging in Aktualizr"
DESCRIPTION = "Configures aktualizr to log at a debugging level"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

SRC_URI = " \
            file://LICENSE \
            file://90-log-debug.toml \
            "

do_install_append () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/90-log-debug.toml ${D}${libdir}/sota/conf.d/90-log-debug.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d/90-log-debug.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:

