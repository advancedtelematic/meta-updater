SUMMARY = "Aktualizr configuration for shared credential provisioning"
DESCRIPTION = "Configuration for provisioning Aktualizr with shared credentials"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch

DEPENDS = "aktualizr-native zip-native"
RDEPENDS_${PN}_append = "${@' aktualizr-shared-prov-creds' if d.getVar('SOTA_DEPLOY_CREDENTIALS', True) == '1' else ''}"
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
    install -m 0644 ${STAGING_DIR_NATIVE}${libdir}/sota/sota-shared-cred.toml \
        ${D}${libdir}/sota/conf.d/20-sota-shared-cred.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/20-sota-shared-cred.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
