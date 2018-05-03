SUMMARY = "Aktualizr configuration for implicit provisioning with CA"
DESCRIPTION = "Systemd service and configurations for implicitly provisioning Aktualizr using externally provided or generated CA"

# WARNING: it is NOT a production solution. The secure way to provision devices is to create certificate request directly on the device
#  (either with HSM/TPM or with software) and then sign it with a CA stored on a disconnected machine

HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "aktualizr-native openssl-native"
RDEPENDS_${PN} = "aktualizr"

SRC_URI = " \
  file://LICENSE \
  file://ca.cnf \
  "
PV = "1.0"
PR = "1"

require environment.inc
require credentials.inc

export SOTA_CACERT_PATH
export SOTA_CAKEY_PATH

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d

    if [ -z "${SOTA_PACKED_CREDENTIALS}" ]; then
        bberror "SOTA_PACKED_CREDENTIALS are required for implicit provisioning"
    fi

    if [ -z ${SOTA_CACERT_PATH} ]; then
        SOTA_CACERT_PATH=${DEPLOY_DIR_IMAGE}/CA/cacert.pem
        SOTA_CAKEY_PATH=${DEPLOY_DIR_IMAGE}/CA/ca.private.pem
        mkdir -p ${DEPLOY_DIR_IMAGE}/CA
        bbwarn "SOTA_CACERT_PATH is not specified, use default one at $SOTA_CACERT_PATH" 

        if [ ! -f ${SOTA_CACERT_PATH} ]; then
            bbwarn "${SOTA_CACERT_PATH} does not exist, generate a new CA"
            SOTA_CACERT_DIR_PATH="$(dirname "$SOTA_CACERT_PATH")"
            openssl genrsa -out ${SOTA_CACERT_DIR_PATH}/ca.private.pem 4096
            openssl req -key ${SOTA_CACERT_DIR_PATH}/ca.private.pem -new -x509 -days 7300 -out ${SOTA_CACERT_PATH} -subj "/C=DE/ST=Berlin/O=Reis und Kichererbsen e.V/commonName=meta-updater" -batch -config ${WORKDIR}/ca.cnf -extensions cacert
            bbwarn "${SOTA_CACERT_PATH} has been created, you'll need to upload it to the server"
        fi
    fi

    if [ -z ${SOTA_CAKEY_PATH} ]; then
        bberror "SOTA_CAKEY_PATH should be set when using implicit provisioning"
    fi

    install -m 0700 -d ${D}${localstatedir}/sota
    install -m 0644 ${STAGING_DIR_NATIVE}${libdir}/sota/sota_implicit_prov_ca.toml ${D}${libdir}/sota/conf.d/20-sota.toml
    aktualizr_cert_provider --credentials ${SOTA_PACKED_CREDENTIALS} \
                            --device-ca ${SOTA_CACERT_PATH} \
                            --device-ca-key ${SOTA_CAKEY_PATH} \
                            --root-ca \
                            --server-url \
                            --local ${D}${localstatedir}/sota \
                            --config ${D}${libdir}/sota/conf.d/20-sota.toml
}

FILES_${PN} = " \
                ${localstatedir}/sota/* \
                ${libdir}/sota/conf.d/20-sota.toml \
                ${libdir}/sota/root.crt \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
