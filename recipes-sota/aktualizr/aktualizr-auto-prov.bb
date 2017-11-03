SUMMARY = "Aktualizr systemd service and configurations"
DESCRIPTION = "Systemd service and configurations for autoprovisioning Aktualizr, the SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
DEPENDS = "zip-native"
RDEPENDS_${PN} = "aktualizr"
PV = "1.0"
PR = "6"

SRC_URI = " \
  file://LICENSE \
  file://aktualizr-manual-provision.service \
  file://aktualizr-autoprovision.service \
  file://sota_autoprov.toml \
  "

SYSTEMD_SERVICE_${PN} = "aktualizr.service"

inherit systemd

export SOTA_PACKED_CREDENTIALS

do_install_append() {
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

    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        install -d ${D}/${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/aktualizr-autoprovision.service ${D}/${systemd_unitdir}/system/aktualizr.service
        install -d ${D}${libdir}/sota
        install -m "0644" ${WORKDIR}/sota_autoprov.toml ${D}${libdir}/sota/sota.toml

      # deploy SOTA credentials
      if [ -e ${SOTA_PACKED_CREDENTIALS} ]; then
          mkdir -p ${D}/var/sota
          cp ${SOTA_PACKED_CREDENTIALS} ${D}/var/sota/sota_provisioning_credentials.zip
          # Device should not be able to push data to treehub
          zip -d ${D}/var/sota/sota_provisioning_credentials.zip treehub.json
      fi
    else
        install -d ${D}/${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/aktualizr-manual-provision.service ${D}/${systemd_unitdir}/system/aktualizr.service
    fi
}

FILES_${PN} = " \
                ${systemd_unitdir}/system/aktualizr.service \
                ${libdir}/sota/sota.toml \
                /var/sota/sota_provisioning_credentials.zip \
                "
