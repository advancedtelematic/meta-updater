SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "boost curl openssl libarchive libsodium asn1c-native "
DEPENDS_append_class-target = "ostree ${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', ' libp11', '', d)} "
DEPENDS_append_class-native = "glib-2.0-native "

RDEPENDS_${PN}_class-target = "lshw "
RDEPENDS_${PN}_append_class-target = " ${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'serialcan', '  slcand-start', '', d)} "

PV = "1.0+git${SRCPV}"
PR = "7"

SRC_URI = " \
  gitsm://github.com/advancedtelematic/aktualizr;branch=${BRANCH} \
  file://aktualizr.service \
  file://aktualizr-secondary.service \
  file://aktualizr-secondary.socket \
  file://aktualizr-serialcan.service \
  "
SRCREV = "930d8eef6eb584686654601c056d7c9c6fca3048"
BRANCH ?= "master"

S = "${WORKDIR}/git"

inherit cmake

inherit systemd

SYSTEMD_PACKAGES = "${PN} ${PN}-secondary"
SYSTEMD_SERVICE_${PN} = "aktualizr.service"
SYSTEMD_SERVICE_${PN}-secondary = "aktualizr-secondary.socket"

BBCLASSEXTEND =+ "native"

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DAKTUALIZR_VERSION=${PV} "
EXTRA_OECMAKE_append_class-target = " -DBUILD_OSTREE=ON -DBUILD_ISOTP=ON ${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', '-DBUILD_P11=ON', '', d)} "
EXTRA_OECMAKE_append_class-native = " -DBUILD_SOTA_TOOLS=ON -DBUILD_OSTREE=OFF -DBUILD_SYSTEMD=OFF "

do_install_append () {
    rm -fr ${D}${libdir}/systemd
    rm -f ${D}${libdir}/sota/sota.toml # Only needed for the Debian package
    install -d ${D}${libdir}/sota
    install -m 0644 ${S}/config/sota_autoprov.toml ${D}/${libdir}/sota/sota_autoprov.toml
    install -m 0644 ${S}/config/sota_autoprov_primary.toml ${D}/${libdir}/sota/sota_autoprov_primary.toml
    install -m 0644 ${S}/config/sota_hsm_prov.toml ${D}/${libdir}/sota/sota_hsm_prov.toml
    install -m 0644 ${S}/config/sota_implicit_prov.toml ${D}/${libdir}/sota/sota_implicit_prov.toml
    install -m 0644 ${S}/config/sota_implicit_prov_ca.toml ${D}/${libdir}/sota/sota_implicit_prov_ca.toml
    install -m 0644 ${S}/config/sota_secondary.toml ${D}/${libdir}/sota/sota_secondary.toml
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/aktualizr-secondary.socket ${D}${systemd_unitdir}/system/aktualizr-secondary.socket
    install -m 0644 ${WORKDIR}/aktualizr-secondary.service ${D}${systemd_unitdir}/system/aktualizr-secondary.service
}

do_install_append_class-target () {
    install -d ${D}${systemd_unitdir}/system
    aktualizr_service=${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'serialcan', '${WORKDIR}/aktualizr-serialcan.service', '${WORKDIR}/aktualizr.service', d)}
    install -m 0644 ${aktualizr_service} ${D}${systemd_unitdir}/system/aktualizr.service
}

do_install_append_class-native () {
    install -m 0755 ${B}/src/sota_tools/garage-sign-prefix/src/garage-sign/bin/* ${D}${bindir}
    install -m 0644 ${B}/src/sota_tools/garage-sign-prefix/src/garage-sign/lib/* ${D}${libdir}
}

PACKAGES =+ " ${PN}-common ${PN}-examples ${PN}-host-tools ${PN}-secondary "

FILES_${PN} = " \
                ${bindir}/aktualizr \
                ${bindir}/aktualizr-info \
                ${bindir}/aktualizr-check-discovery \
                ${systemd_unitdir}/system/aktualizr.service \
                "

FILES_${PN}-common = " \
                ${libdir}/sota/schemas \
                "

FILES_${PN}-examples = " \
                ${libdir}/sota/demo_secondary.json \
                ${bindir}/example-interface \
                ${bindir}/isotp-test-interface \
                "

FILES_${PN}-host-tools = " \
                ${bindir}/aktualizr_cert_provider \
                ${bindir}/aktualizr_implicit_writer \
                ${bindir}/garage-deploy \
                ${bindir}/garage-push \
                ${libdir}/sota/sota_autoprov.toml \
                ${libdir}/sota/sota_autoprov_primary.toml \
                ${libdir}/sota/sota_hsm_prov.toml \
                ${libdir}/sota/sota_implicit_prov.toml \
                ${libdir}/sota/sota_implicit_prov_ca.toml \
                "

FILES_${PN}-secondary = " \
                ${bindir}/aktualizr-secondary \
                ${libdir}/sota/sota_secondary.toml \
                ${systemd_unitdir}/system/aktualizr-secondary.socket \
                ${systemd_unitdir}/system/aktualizr-secondary.service \
                "

# Both primary and secondary need the SQL Schemas
RDEPENDS_${PN}_class-target =+ "${PN}-common"
RDEPENDS_${PN}-secondary_class-target =+ "${PN}-common"

# vim:set ts=4 sw=4 sts=4 expandtab:
