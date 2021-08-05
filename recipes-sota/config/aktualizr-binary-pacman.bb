DESCRIPTION = "Configure aktualizr with a binary package manager"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch

SRC_URI = "\
    file://10-pacman.toml \
    "

FILES:${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/10-pacman.toml \
              "

PR = "1"

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/10-pacman.toml ${D}${libdir}/sota/conf.d/10-pacman.toml
}
