SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

DEPENDS = "boost curl openssl libarchive libsodium sqlite3 asn1c-native"
DEPENDS_append = "${@bb.utils.contains('PTEST_ENABLED', '1', ' coreutils-native ostree-native aktualizr-native ', '', d)}"
RDEPENDS_${PN}_class-target = "aktualizr-check-discovery aktualizr-configs lshw"
RDEPENDS_${PN}-secondary = "aktualizr-check-discovery"
RDEPENDS_${PN}-host-tools = "aktualizr aktualizr-repo aktualizr-cert-provider ${@bb.utils.contains('PACKAGECONFIG', 'sota-tools', 'garage-deploy garage-push', '', d)}"

PV = "1.0+git${SRCPV}"
PR = "7"

GARAGE_SIGN_PV = "0.6.0-3-gc38b9f3"

SRC_URI = " \
  gitsm://github.com/advancedtelematic/aktualizr;branch=${BRANCH} \
  file://aktualizr.service \
  file://aktualizr-secondary.service \
  file://aktualizr-secondary.socket \
  file://aktualizr-serialcan.service \
  ${@ d.expand("https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/cli-${GARAGE_SIGN_PV}.tgz;unpack=0") if d.getVar('GARAGE_SIGN_AUTOVERSION') != '1' else ''} \
  "

# for garage-sign archive
SRC_URI[md5sum] = "30d7f0931e2236954679e75d1bae174f"
SRC_URI[sha256sum] = "46d8c6448ce14cbb9af6a93eba7e29d38579e566dcd6518d22f723a8da16cad5"

SRCREV = "c71ec0a320d85a3e75ba37bff7dc40ad02e9d655"
BRANCH ?= "master"

S = "${WORKDIR}/git"

inherit cmake pkgconfig ptest systemd

SYSTEMD_PACKAGES = "${PN} ${PN}-secondary"
SYSTEMD_SERVICE_${PN} = "aktualizr.service"
SYSTEMD_SERVICE_${PN}-secondary = "aktualizr-secondary.socket"

EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Release -DAKTUALIZR_VERSION=${PV} -Dgtest_disable_pthreads=ON"

GARAGE_SIGN_OPS = "${@ d.expand('-DGARAGE_SIGN_ARCHIVE=${WORKDIR}/cli-${GARAGE_SIGN_PV}.tgz') if d.getVar('GARAGE_SIGN_AUTOVERSION') != '1' else ''}"

PACKAGECONFIG ?= "ostree ${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)} ${@bb.utils.filter('SOTA_CLIENT_FEATURES', 'hsm serialcan ubootenv', d)}"
PACKAGECONFIG_class-native = "sota-tools"
PACKAGECONFIG[warning-as-error] = "-DWARNING_AS_ERROR=ON,-DWARNING_AS_ERROR=OFF,"
PACKAGECONFIG[ostree] = "-DBUILD_OSTREE=ON,-DBUILD_OSTREE=OFF,ostree,"
PACKAGECONFIG[hsm] = "-DBUILD_P11=ON,-DBUILD_P11=OFF,libp11,"
PACKAGECONFIG[sota-tools] = "-DBUILD_SOTA_TOOLS=ON ${GARAGE_SIGN_OPS},-DBUILD_SOTA_TOOLS=OFF,glib-2.0,"
PACKAGECONFIG[systemd] = "-DBUILD_SYSTEMD=ON,-DBUILD_SYSTEMD=OFF,systemd,"
PACKAGECONFIG[load-tests] = "-DBUILD_LOAD_TESTS=ON,-DBUILD_LOAD_TESTS=OFF,"
PACKAGECONFIG[serialcan] = ",,,slcand-start"
PACKAGECONFIG[ubootenv] = ",,,u-boot-fw-utils aktualizr-uboot-env-rollback"

do_compile_ptest() {
    cmake_runcmake_build --target build_tests
}

do_install_append () {
    install -d ${D}${libdir}/sota
    install -m 0644 ${S}/config/sota_autoprov.toml ${D}/${libdir}/sota/sota_autoprov.toml
    install -m 0644 ${S}/config/sota_autoprov_primary.toml ${D}/${libdir}/sota/sota_autoprov_primary.toml
    install -m 0644 ${S}/config/sota_hsm_prov.toml ${D}/${libdir}/sota/sota_hsm_prov.toml
    install -m 0644 ${S}/config/sota_implicit_prov_ca.toml ${D}/${libdir}/sota/sota_implicit_prov_ca.toml
    install -m 0644 ${S}/config/sota_secondary.toml ${D}/${libdir}/sota/sota_secondary.toml
    install -m 0644 ${S}/config/sota_uboot_env.toml ${D}/${libdir}/sota/sota_uboot_env.toml
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/aktualizr-secondary.socket ${D}${systemd_unitdir}/system/aktualizr-secondary.socket
    install -m 0644 ${WORKDIR}/aktualizr-secondary.service ${D}${systemd_unitdir}/system/aktualizr-secondary.service
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0700 -d ${D}${sysconfdir}/sota/conf.d

    if [ -n "${SOTA_HARDWARE_ID}" ]; then
        printf "[provision]\nprimary_ecu_hardware_id = ${SOTA_HARDWARE_ID}\n" > ${D}${libdir}/sota/conf.d/40-hardware-id.toml
    fi

    if [ -n "${SOTA_SECONDARY_CONFIG_DIR}" ]; then
        if [ -d "${SOTA_SECONDARY_CONFIG_DIR}" ]; then
            install -m 0700 -d ${D}${sysconfdir}/sota/ecus
            install -m 0644 "${SOTA_SECONDARY_CONFIG_DIR}"/* ${D}${sysconfdir}/sota/ecus/
            printf "[uptane]\nsecondary_configs_dir = /etc/sota/ecus/\n" > ${D}${libdir}/sota/conf.d/30-secondary-configs-dir.toml
        else
            bbwarn "SOTA_SECONDARY_CONFIG_DIR is set to an invalid directory (${SOTA_SECONDARY_CONFIG_DIR})"
        fi
    fi

    install -m 0755 -d ${D}${systemd_unitdir}/system
    aktualizr_service=${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'serialcan', '${WORKDIR}/aktualizr-serialcan.service', '${WORKDIR}/aktualizr.service', d)}
    install -m 0644 ${aktualizr_service} ${D}${systemd_unitdir}/system/aktualizr.service

    if ${@bb.utils.contains('PACKAGECONFIG', 'sota-tools', 'true', 'false', d)}; then
        install -m 0755 ${B}/src/sota_tools/garage-sign/bin/* ${D}${bindir}
        install -m 0644 ${B}/src/sota_tools/garage-sign/lib/* ${D}${libdir}
    fi
}

PACKAGESPLITFUNCS_prepend = "split_hosttools_packages "

python split_hosttools_packages () {
    bindir = d.getVar('bindir')

    # Split all binaries to their own packages except aktualizr-info,
    # aktualizr-info should stay in main package aktualizr.
    do_split_packages(d, bindir, r'^((?!(aktualizr-info)).*)$', '%s', 'Aktualizr tool - %s', extra_depends='aktualizr-configs', prepend=False)
}

PACKAGES_DYNAMIC = "^aktualizr-.* ^garage-.*"

PACKAGES =+ "${PN}-examples ${PN}-secondary ${PN}-configs ${PN}-host-tools"

ALLOW_EMPTY_${PN}-host-tools = "1"

FILES_${PN} = " \
                ${bindir}/aktualizr \
                ${bindir}/aktualizr-info \
                ${systemd_unitdir}/system/aktualizr.service \
                "

FILES_${PN}-configs = " \
                ${sysconfdir}/sota/* \
                ${libdir}/sota/* \
                "

FILES_${PN}-examples = " \
                ${bindir}/hmi-stub \
                "

FILES_${PN}-secondary = " \
                ${bindir}/aktualizr-secondary \
                ${libdir}/sota/sota_secondary.toml \
                ${systemd_unitdir}/system/aktualizr-secondary.socket \
                ${systemd_unitdir}/system/aktualizr-secondary.service \
                "
BBCLASSEXTEND = "native"

# vim:set ts=4 sw=4 sts=4 expandtab:
