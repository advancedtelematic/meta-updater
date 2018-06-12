SUMMARY = "Aktualizr configuration with HSM support"
DESCRIPTION = "Configuration for HSM provisioning with Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "aktualizr aktualizr-native"

SRC_URI = " \
  file://LICENSE \
  "
PV = "1.0"
PR = "6"

require environment.inc
require credentials.inc

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${STAGING_DIR_HOST}${libdir}/sota/sota_hsm_prov.toml \
        ${D}${libdir}/sota/conf.d/20-sota_hsm_prov.toml
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        aktualizr_implicit_writer -c ${SOTA_PACKED_CREDENTIALS} --no-root-ca \
            -o ${D}${libdir}/sota/conf.d/30-implicit_server.toml -p ${D}
    fi
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/20-sota_hsm_prov.toml \
                ${libdir}/sota/conf.d/30-implicit_server.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
