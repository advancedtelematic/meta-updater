SUMMARY = "Enable auto reboot to apply updates"
DESCRIPTION = "Configures aktualizr to auto reboot just after new updates installation in order to apply them"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

SRC_URI = " \
            file://40-enable-auto-reboot.toml \
            "

do_install_append () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/40-enable-auto-reboot.toml ${D}${libdir}/sota/conf.d/40-enable-auto-reboot.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d/40-enable-auto-reboot.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:

