SUMMARY = "Aktualizr configuration with HSM support"
DESCRIPTION = "Systemd service and configurations for HSM provisioning with Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "aktualizr-native"
RDEPENDS_${PN} = "aktualizr softhsm softhsm-testtoken"

SRC_URI = " \
  file://LICENSE \
  "
PV = "1.0"
PR = "6"

require environment.inc
require credentials.inc

do_install() {
    install -d ${D}${libdir}/sota
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        aktualizr_implicit_writer -c ${SOTA_PACKED_CREDENTIALS} --no-root-ca \
            -i ${STAGING_DIR_NATIVE}${libdir}/sota/sota_hsm_prov.toml -o ${D}${libdir}/sota/sota.toml -p ${D}
    fi

    if ${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'disable_send_ip', 'true', 'false', d)}; then
      cat << EOF >> ${D}${libdir}/sota/sota.toml

[telemetry]
report_network = false
EOF
    fi

}

FILES_${PN} = " \
                ${libdir}/sota/sota.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
