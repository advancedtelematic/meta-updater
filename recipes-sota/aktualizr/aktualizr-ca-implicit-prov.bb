SUMMARY = "Aktualizr configuration for implicit provisioning with CA"
DESCRIPTION = "Configuration for implicitly provisioning Aktualizr using externally provided or generated CA"

# WARNING: it is NOT a production solution. The secure way to provision devices is to create certificate request directly on the device
#  (either with HSM/TPM or with software) and then sign it with a CA stored on a disconnected machine

HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

DEPENDS = "aktualizr aktualizr-native openssl-native"
RDEPENDS_${PN}_append = "${@' aktualizr-ca-implicit-prov-creds' if d.getVar('SOTA_DEPLOY_CREDENTIALS', True) == '1' else ''}"

PV = "1.0"
PR = "1"

require credentials.inc

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d

    install -m 0644 ${STAGING_DIR_HOST}${libdir}/sota/sota_implicit_prov_ca.toml \
        ${D}${libdir}/sota/conf.d/20-sota_implicit_prov_ca.toml
}

FILES_${PN} = " \
                ${libdir}/sota/conf.d/20-sota_implicit_prov_ca.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
