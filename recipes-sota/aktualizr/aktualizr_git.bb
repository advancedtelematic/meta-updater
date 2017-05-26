SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"
DEPENDS = "boost curl openssl jansson libsodium ostree"
SRCREV = "4e9344ae375a444f02b964dca52fe808010d17df"
PV = "1.0+git${SRCPV}"

SRC_URI = " \
  git://github.com/advancedtelematic/aktualizr \
  file://aktualizr-manual-provision.service \
  "

S = "${WORKDIR}/git"
SYSTEMD_SERVICE_${PN} = "aktualizr.service"

inherit cmake systemd

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTS=OFF -DBUILD_OSTREE=ON"

export SOTA_AUTOPROVISION_CREDENTIALS

do_install_append() {
    if [ -n "$SOTA_AUTOPROVISION_CREDENTIALS" ]; then
      bbwarn "Aktualizr recipe currently lacks support for SOTA_AUTOPROVISION_CREDENTIALS. No systemd service will be created"
    else
      install -d ${D}/${systemd_unitdir}/system
      install -m 0644 ${WORKDIR}/aktualizr-manual-provision.service ${D}/${systemd_unitdir}/system/aktualizr.service
    fi
}

RDEPENDS = ""

FILES_${PN} = " \
                ${bindir}/aktualizr \
                ${systemd_unitdir}/system/aktualizr.service \
                "
