SUMMARY = "Aktualizr configuration snippet to enable uboot bootcount function"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"
DEPENDS = "aktualizr-native"
RDEPENDS_${PN} = "aktualizr"

SRC_URI = ""


do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${STAGING_DIR_NATIVE}${libdir}/sota/sota_uboot_env.toml ${D}${libdir}/sota/conf.d/30-rollback.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/30-rollback.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
