SUMMARY = "Example virtual secondary in aktualizr"
DESCRIPTION = "Creates an example virtual secondary to be used to update an arbitrary file on the primary"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch

SRC_URI = " \
            file://30-virtualsec.toml \
            file://virtualsec.json \
            "

do_install:append () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/30-virtualsec.toml ${D}${libdir}/sota/conf.d/30-virtualsec.toml
    install -m 0644 ${WORKDIR}/virtualsec.json ${D}${libdir}/sota/virtualsec.json
}

FILES:${PN} = " \
                ${libdir}/sota/conf.d/30-virtualsec.toml \
                ${libdir}/sota/virtualsec.json \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:

