DESCRIPTION = "Sample configuration for an Uptane Secondary"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

require shared-conf.inc

SECONDARY_SERIAL_ID ?= ""
SOTA_HARDWARE_ID ?= "${MACHINE}-sndry"
SECONDARY_HARDWARE_ID ?= "${SOTA_HARDWARE_ID}"

SRC_URI = "\
    file://30-fake_pacman.toml \
    file://35-network_config.toml \
    file://45-id_config.toml \
    "

do_install () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/30-fake_pacman.toml ${D}/${libdir}/sota/conf.d/30-fake_pacman.toml

    install -m 0644 ${WORKDIR}/35-network_config.toml ${D}/${libdir}/sota/conf.d/35-network_config.toml
    sed -i -e 's|@PORT@|${SECONDARY_PORT}|g' \
           -e 's|@PRIMARY_IP@|${PRIMARY_IP}|g' \
           -e 's|@PRIMARY_PORT@|${PRIMARY_PORT}|g' \
           ${D}/${libdir}/sota/conf.d/35-network_config.toml

    install -m 0644 ${WORKDIR}/45-id_config.toml ${D}/${libdir}/sota/conf.d/45-id_config.toml
    sed -i -e 's|@SERIAL@|${SECONDARY_SERIAL_ID}|g' \
           -e 's|@HWID@|${SECONDARY_HARDWARE_ID}|g' \
           ${D}/${libdir}/sota/conf.d/45-id_config.toml

}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/30-fake_pacman.toml \
                ${libdir}/sota/conf.d/35-network_config.toml \
                ${libdir}/sota/conf.d/45-id_config.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
