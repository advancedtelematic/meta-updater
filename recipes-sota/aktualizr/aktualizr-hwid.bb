SUMMARY = "Aktualizr hwid configuration"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

# Because of the dependency on MACHINE.
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = ""

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    if [ -n "${SOTA_HARDWARE_ID}" ]; then
        printf "[provision]\nprimary_ecu_hardware_id = ${SOTA_HARDWARE_ID}\n" > ${D}${libdir}/sota/conf.d/40-hardware-id.toml
    fi
}

FILES:${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/40-hardware-id.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
