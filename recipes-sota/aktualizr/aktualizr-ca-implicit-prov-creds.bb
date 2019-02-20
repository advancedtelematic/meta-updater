SUMMARY = "Credentials for implicit provisioning with CA certificate"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

DEPENDS = "aktualizr aktualizr-native"
ALLOW_EMPTY_${PN} = "1"

SRC_URI = " \
  file://ca.cnf \
  "

require credentials.inc

export SOTA_CACERT_PATH
export SOTA_CAKEY_PATH

do_install() {
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        if [ -z ${SOTA_CACERT_PATH} ]; then
            SOTA_CACERT_PATH=${DEPLOY_DIR_IMAGE}/CA/cacert.pem
            SOTA_CAKEY_PATH=${DEPLOY_DIR_IMAGE}/CA/ca.private.pem
            mkdir -p ${DEPLOY_DIR_IMAGE}/CA
            bbwarn "SOTA_CACERT_PATH is not specified, use default one at ${SOTA_CACERT_PATH}"

            if [ ! -f ${SOTA_CACERT_PATH} ]; then
                bbwarn "${SOTA_CACERT_PATH} does not exist, generate a new CA"
                SOTA_CACERT_DIR_PATH="$(dirname "${SOTA_CACERT_PATH}")"
                openssl genrsa -out ${SOTA_CACERT_DIR_PATH}/ca.private.pem 4096
                openssl req -key ${SOTA_CACERT_DIR_PATH}/ca.private.pem -new -x509 -days 7300 -out ${SOTA_CACERT_PATH} -subj "/C=DE/ST=Berlin/O=Reis und Kichererbsen e.V/commonName=meta-updater" -batch -config ${WORKDIR}/ca.cnf -extensions cacert
                bbwarn "${SOTA_CACERT_PATH} has been created, you'll need to upload it to the server"
            fi
        fi

        if [ -z ${SOTA_CAKEY_PATH} ]; then
            bbfatal "SOTA_CAKEY_PATH should be set when using implicit provisioning"
        fi

        install -m 0700 -d ${D}${localstatedir}/sota
        aktualizr-cert-provider --credentials ${SOTA_PACKED_CREDENTIALS} \
                                --fleet-ca ${SOTA_CACERT_PATH} \
                                --fleet-ca-key ${SOTA_CAKEY_PATH} \
                                --root-ca \
                                --server-url \
                                --local ${D} \
                                --config ${STAGING_DIR_HOST}${libdir}/sota/sota_implicit_prov_ca.toml
    fi
}

FILES_${PN} = " \
                ${localstatedir}/sota/*"
