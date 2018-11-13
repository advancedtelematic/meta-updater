SUMMARY = "Aktualizr configuration for autoprovisioning"
DESCRIPTION = "Configuration for automatically provisioning Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

DEPENDS = "aktualizr-native zip-native"
RDEPENDS_${PN}_append = "${@' aktualizr-auto-prov-creds' if d.getVar('SOTA_DEPLOY_CREDENTIALS', True) == '1' else ''}"
PV = "1.0"
PR = "6"

SRC_URI = ""

require credentials.inc

do_install() {
    if [ -n "${SOTA_AUTOPROVISION_CREDENTIALS}" ]; then
        bbwarn "SOTA_AUTOPROVISION_CREDENTIALS are ignored. Please use SOTA_PACKED_CREDENTIALS"
    fi
    if [ -n "${SOTA_AUTOPROVISION_URL}" ]; then
        bbwarn "SOTA_AUTOPROVISION_URL is ignored. Please use SOTA_PACKED_CREDENTIALS"
    fi
    if [ -n "${SOTA_AUTOPROVISION_URL_FILE}" ]; then
        bbwarn "SOTA_AUTOPROVISION_URL_FILE is ignored. Please use SOTA_PACKED_CREDENTIALS"
    fi
    if [ -n "${OSTREE_PUSH_CREDENTIALS}" ]; then
        bbwarn "OSTREE_PUSH_CREDENTIALS is ignored. Please use SOTA_PACKED_CREDENTIALS"
    fi

    install -m 0700 -d ${D}${libdir}/sota/conf.d
    aktualizr_toml=${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'secondary-network', 'sota_autoprov_primary.toml', 'sota_autoprov.toml', d)}

    install -m 0644 ${STAGING_DIR_NATIVE}${libdir}/sota/${aktualizr_toml} \
        ${D}${libdir}/sota/conf.d/20-${aktualizr_toml}
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/20-${aktualizr_toml} \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
