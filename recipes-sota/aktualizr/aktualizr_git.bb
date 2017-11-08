SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
DEPENDS = "boost curl openssl jansson libsodium ostree"
RDEPENDS_${PN} = "lshw"

SRC_URI = " \
  git://github.com/advancedtelematic/aktualizr \
  file://aktualizr-manual-provision.service \
  file://aktualizr-autoprovision.service \
  file://sota_autoprov.toml \
  "
SRCREV = "1004efa3f86cef90c012b34620992b5762b741e3"
PV = "1.0+git${SRCPV}"
PR = "6"

S = "${WORKDIR}/git"
SYSTEMD_SERVICE_${PN} = "aktualizr.service"

inherit cmake systemd

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTS=OFF -DBUILD_OSTREE=ON -DAKTUALIZR_VERSION=${PV}"

export SOTA_PACKED_CREDENTIALS

do_install_append() {
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
      install -d ${D}/${systemd_unitdir}/system
      install -m 0644 ${WORKDIR}/aktualizr-autoprovision.service ${D}/${systemd_unitdir}/system/aktualizr.service
      install -d ${D}/usr/lib/sota
      install -m "0644" ${WORKDIR}/sota_autoprov.toml ${D}/usr/lib/sota/sota.toml
    else
      install -d ${D}/${systemd_unitdir}/system
      install -m 0644 ${WORKDIR}/aktualizr-manual-provision.service ${D}/${systemd_unitdir}/system/aktualizr.service
    fi

    rm -f ${D}${bindir}/aktualizr_cert_provider
    rm -f ${D}${bindir}/aktualizr_implicit_writer
}

FILES_${PN} = " \
                ${bindir}/aktualizr \
                ${systemd_unitdir}/system/aktualizr.service \
                /usr/lib/sota/sota.toml \
                "
