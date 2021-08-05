SUMMARY = "Set polling interval in Aktualizr"
DESCRIPTION = "Configures aktualizr to poll at a custom frequency (suitable for testing or other purposes)"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch

SRC_URI = " \
            file://60-polling-interval.toml \
            "

SOTA_POLLING_SEC ?= "30"

do_install:append () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/60-polling-interval.toml ${D}${libdir}/sota/conf.d/60-polling-interval.toml

    sed -i -e 's|@POLLING_SEC@|${SOTA_POLLING_SEC}|g' \
           ${D}${libdir}/sota/conf.d/60-polling-interval.toml
}

FILES:${PN} = " \
                ${libdir}/sota/conf.d/60-polling-interval.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:

