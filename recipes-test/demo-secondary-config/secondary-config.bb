DESCRIPTION = "Sample configuration for an Uptane Secondary"
LICENSE = "CLOSED"

inherit allarch

SRC_URI = "\
    file://30-fake_pacman.toml \
    "

do_install () {
    install -m 0700 -d ${D}${libdir}/sota/conf.d 
    install -m 0644 ${WORKDIR}/30-fake_pacman.toml ${D}/${libdir}/sota/conf.d/30-fake_pacman.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/30-fake_pacman.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
