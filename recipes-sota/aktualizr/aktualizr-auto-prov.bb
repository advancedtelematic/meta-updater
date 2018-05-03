SUMMARY = "Aktualizr configuration for autoprovisioning"
DESCRIPTION = "Systemd service and configurations for autoprovisioning Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
DEPENDS = "aktualizr-native zip-native"
RDEPENDS_${PN} = "aktualizr"
PV = "1.0"
PR = "6"

SRC_URI = " \
  file://LICENSE \
  "

require environment.inc
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
    install -m 0700 -d ${D}${localstatedir}/sota
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        aktualizr_toml=${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'secondary-network', 'sota_autoprov_primary.toml', 'sota_autoprov.toml', d)}

        install -m 0644 ${STAGING_DIR_NATIVE}${libdir}/sota/${aktualizr_toml} ${D}${libdir}/sota/conf.d/20-sota.toml

        # deploy SOTA credentials
        if [ -e ${SOTA_PACKED_CREDENTIALS} ]; then
            cp ${SOTA_PACKED_CREDENTIALS} ${D}${localstatedir}/sota/sota_provisioning_credentials.zip
            # Device should not be able to push data to treehub
            zip -d ${D}${localstatedir}/sota/sota_provisioning_credentials.zip treehub.json
        fi
    fi
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d/20-sota.toml \
                ${localstatedir}/sota \
                ${localstatedir}/sota/sota_provisioning_credentials.zip \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
