SUMMARY = "Aktualizr configuration with HSM support"
DESCRIPTION = "Configuration for HSM provisioning with Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

DEPENDS = "aktualizr aktualizr-native"
RDEPENDS_${PN}_append = "${@' aktualizr-ca-implicit-prov-creds softhsm-testtoken' if d.getVar('SOTA_DEPLOY_CREDENTIALS', True) == '1' else ''}"

SRC_URI = ""
PV = "1.0"
PR = "6"

require credentials.inc

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${STAGING_DIR_HOST}${libdir}/sota/sota_hsm_prov.toml \
        ${D}${libdir}/sota/conf.d/20-sota_hsm_prov.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/20-sota_hsm_prov.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
