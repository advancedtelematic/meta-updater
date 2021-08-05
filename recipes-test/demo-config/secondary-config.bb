DESCRIPTION = "Sample configuration for an Uptane Secondary"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

require shared-conf.inc

# Because of the dependency on MACHINE.
PACKAGE_ARCH = "${MACHINE_ARCH}"

SECONDARY_SERIAL_ID ?= ""
SOTA_HARDWARE_ID ?= "${MACHINE}-sndry"
SECONDARY_HARDWARE_ID ?= "${SOTA_HARDWARE_ID}"
SECONDARY_UPDATE_TYPE ?= "ostree"

UPDATE_TYPE = "${SECONDARY_UPDATE_TYPE}"
python () {
    update_type = d.getVar('UPDATE_TYPE')
    if update_type not in [ 'ostree', 'file']:
        bb.fatal('Unsupported type of an update specified for secondary: SECONDARY_UPDATE_TYPE = {}\n'
                  'Supported update types are: ostree and file'
        .format(update_type))

    if update_type == 'file':
        d.setVar('UPDATE_TYPE', 'none')
}

SRC_URI = "\
    file://30-pacman-config.toml \
    file://35-network-config.toml \
    file://45-id-config.toml \
    "


do_install () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d

    install -m 0644 ${WORKDIR}/30-pacman-config.toml ${D}${libdir}/sota/conf.d/30-pacman-config.toml
    sed -i -e 's|@UPDATE_TYPE@|${UPDATE_TYPE}|g' ${D}${libdir}/sota/conf.d/30-pacman-config.toml

    install -m 0644 ${WORKDIR}/35-network-config.toml ${D}${libdir}/sota/conf.d/35-network-config.toml
    sed -i -e 's|@PORT@|${SECONDARY_PORT}|g' \
           -e 's|@PRIMARY_IP@|${PRIMARY_IP}|g' \
           -e 's|@PRIMARY_PORT@|${PRIMARY_PORT}|g' \
           ${D}${libdir}/sota/conf.d/35-network-config.toml

    install -m 0644 ${WORKDIR}/45-id-config.toml ${D}${libdir}/sota/conf.d/45-id-config.toml
    sed -i -e 's|@SERIAL@|${SECONDARY_SERIAL_ID}|g' \
           -e 's|@HWID@|${SECONDARY_HARDWARE_ID}|g' \
           ${D}${libdir}/sota/conf.d/45-id-config.toml

}

FILES:${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/30-pacman-config.toml \
                ${libdir}/sota/conf.d/35-network-config.toml \
                ${libdir}/sota/conf.d/45-id-config.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
