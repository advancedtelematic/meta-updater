SUMMARY = "Aktualizr configuration snippet to enable uboot bootcount function"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
DEPENDS = "aktualizr-native"
RDEPENDS_${PN} = "aktualizr"

SRC_URI = " \
  file://LICENSE \
  "

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${STAGING_DIR_NATIVE}${libdir}/sota/sota_uboot_env.toml ${D}${libdir}/sota/conf.d/30-rollback.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/30-rollback.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
