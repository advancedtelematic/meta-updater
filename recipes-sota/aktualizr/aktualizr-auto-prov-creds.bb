SUMMARY = "Credentials for autoprovisioning scenario"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

DEPENDS = "aktualizr-native zip-native"
ALLOW_EMPTY_${PN} = "1"

require credentials.inc

do_install() {
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        install -m 0700 -d ${D}${localstatedir}/sota
        cp ${SOTA_PACKED_CREDENTIALS} ${D}${localstatedir}/sota/sota_provisioning_credentials.zip
        # Device should not be able to push data to treehub
        zip -d ${D}${localstatedir}/sota/sota_provisioning_credentials.zip treehub.json
    fi
}

FILES_${PN} = " \
                ${localstatedir}/sota/sota_provisioning_credentials.zip \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
