SUMMARY = "Enable auto reboot to apply updates"
DESCRIPTION = "Configures aktualizr to automatically reboot after new updates are installed in order to apply the updates immediately"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch

SRC_URI = " \
            file://35-enable-auto-reboot.toml \
            "

do_install:append () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/35-enable-auto-reboot.toml ${D}${libdir}/sota/conf.d/35-enable-auto-reboot.toml
}

FILES:${PN} = " \
                ${libdir}/sota/conf.d/35-enable-auto-reboot.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
