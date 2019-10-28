SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=815ca599c9df247a0c7f619bab123dad"

DEPENDS = "boost curl openssl libarchive libsodium sqlite3 asn1c-native"
DEPENDS_append = "${@bb.utils.contains('PTEST_ENABLED', '1', ' coreutils-native net-tools-native ostree-native aktualizr-native ', '', d)}"
RDEPENDS_${PN}_class-target = "aktualizr-configs aktualizr-hwid lshw"
RDEPENDS_${PN}-host-tools = "aktualizr aktualizr-cert-provider ${@bb.utils.contains('PACKAGECONFIG', 'sota-tools', 'garage-deploy garage-push', '', d)}"

RDEPENDS_${PN}-ptest += "bash cmake curl net-tools python3-core python3-misc python3-modules openssl-bin sqlite3 valgrind"

PV = "1.0+git${SRCPV}"
PR = "7"

GARAGE_SIGN_PV = "0.7.0-49-g5ffd420"

SRC_URI = " \
  gitsm://github.com/advancedtelematic/aktualizr;branch=${BRANCH};name=aktualizr \
  file://run-ptest \
  file://aktualizr.service \
  file://aktualizr-secondary.service \
  file://aktualizr-serialcan.service \
  file://10-resource-control.conf \
  ${@ d.expand("https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/cli-${GARAGE_SIGN_PV}.tgz;unpack=0;name=garagesign") if d.getVar('GARAGE_SIGN_AUTOVERSION') != '1' else ''} \
  "

SRC_URI[garagesign.md5sum] = "de0877ecb693fd48ec11052e51b0ff1a"
SRC_URI[garagesign.sha256sum] = "cf25759574c9c1206835daeaf6fc345f6db7b5ccdb95fb828c86d7451f78f0aa"

SRCREV = "fa59e33208d3b1dc690a30ce8339b3b4162f8022"
BRANCH ?= "master"

S = "${WORKDIR}/git"

inherit cmake pkgconfig ptest systemd

# disable ptest by default as it slows down builds quite a lot
# can be enabled manually by setting 'PTEST_ENABLED_pn-aktualizr' to '1' in local.conf
PTEST_ENABLED = "0"

SYSTEMD_PACKAGES = "${PN} ${PN}-secondary"
SYSTEMD_SERVICE_${PN} = "aktualizr.service"
SYSTEMD_SERVICE_${PN}-secondary = "aktualizr-secondary.service"

EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Release ${@bb.utils.contains('PTEST_ENABLED', '1', '-DTESTSUITE_VALGRIND=on', '', d)}"

GARAGE_SIGN_OPS = "${@ d.expand('-DGARAGE_SIGN_ARCHIVE=${WORKDIR}/cli-${GARAGE_SIGN_PV}.tgz') if d.getVar('GARAGE_SIGN_AUTOVERSION') != '1' else ''}"

PACKAGECONFIG ?= "ostree ${@bb.utils.filter('SOTA_CLIENT_FEATURES', 'hsm serialcan ubootenv', d)}"
PACKAGECONFIG_class-native = "sota-tools"
PACKAGECONFIG[warning-as-error] = "-DWARNING_AS_ERROR=ON,-DWARNING_AS_ERROR=OFF,"
PACKAGECONFIG[ostree] = "-DBUILD_OSTREE=ON,-DBUILD_OSTREE=OFF,ostree,"
PACKAGECONFIG[hsm] = "-DBUILD_P11=ON,-DBUILD_P11=OFF,libp11,"
PACKAGECONFIG[sota-tools] = "-DBUILD_SOTA_TOOLS=ON ${GARAGE_SIGN_OPS},-DBUILD_SOTA_TOOLS=OFF,glib-2.0,"
PACKAGECONFIG[systemd] = "-DBUILD_SYSTEMD=ON,-DBUILD_SYSTEMD=OFF,systemd,"
PACKAGECONFIG[load-tests] = "-DBUILD_LOAD_TESTS=ON,-DBUILD_LOAD_TESTS=OFF,"
PACKAGECONFIG[serialcan] = ",,,slcand-start"
PACKAGECONFIG[ubootenv] = ",,,u-boot-fw-utils aktualizr-uboot-env-rollback"

# can be overriden in configuration with `RESOURCE_xxx_pn-aktualizr`
# see `man systemd.resource-control` for details

# can be used to lower aktualizr priority, default is 100
RESOURCE_CPU_WEIGHT = "100"
# will be slowed down when it reaches 'high', killed when it reaches 'max'
RESOURCE_MEMORY_HIGH = "100M"
RESOURCE_MEMORY_MAX = "80%"

do_configure_prepend() {
    # CMake has trouble finding yocto's git when cross-compiling, let's do this step manually
    cd ${S}
    if [ ! -f VERSION ]; then
        ./scripts/get_version.sh > VERSION
    fi
}

do_install_ptest() {
    # copy the complete source directory (contains build)
    cp -r ${B}/ ${D}/${PTEST_PATH}/build
    cp -r ${S}/ ${D}/${PTEST_PATH}/src

    # remove huge external unused repository
    rm -rf ${D}/${PTEST_PATH}/src/partial/extern/RIOT

    # remove huge build artifacts
    find ${D}/${PTEST_PATH}/build/src -name "*.a" -delete

    # fix the absolute paths
    find ${D}/${PTEST_PATH}/build -name "CMakeFiles" | xargs rm -rf
    find ${D}/${PTEST_PATH}/build -name "*.cmake" -or -name "DartConfiguration.tcl" -or -name "run-valgrind" | xargs sed -e "s|${S}|${PTEST_PATH}/src|g" -e "s|${B}|${PTEST_PATH}/build|g" -e "s|\"--gtest_output[^\"]*\"||g" -i
}

do_install_append () {
    install -d ${D}${libdir}/sota
    install -m 0644 ${S}/config/sota-shared-cred.toml ${D}/${libdir}/sota/sota-shared-cred.toml
    install -m 0644 ${S}/config/sota-device-cred-hsm.toml ${D}/${libdir}/sota/sota-device-cred-hsm.toml
    install -m 0644 ${S}/config/sota-device-cred.toml ${D}/${libdir}/sota/sota-device-cred.toml
    install -m 0644 ${S}/config/sota-secondary.toml ${D}/${libdir}/sota/sota-secondary.toml
    install -m 0644 ${S}/config/sota-uboot-env.toml ${D}/${libdir}/sota/sota-uboot-env.toml
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/aktualizr-secondary.service ${D}${systemd_unitdir}/system/aktualizr-secondary.service
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0700 -d ${D}${sysconfdir}/sota/conf.d

    install -m 0755 -d ${D}${systemd_unitdir}/system
    aktualizr_service=${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'serialcan', '${WORKDIR}/aktualizr-serialcan.service', '${WORKDIR}/aktualizr.service', d)}
    install -m 0644 ${aktualizr_service} ${D}${systemd_unitdir}/system/aktualizr.service

    if ${@bb.utils.contains('PACKAGECONFIG', 'sota-tools', 'true', 'false', d)}; then
        install -m 0755 ${B}/src/sota_tools/garage-sign/bin/* ${D}${bindir}
        install -m 0644 ${B}/src/sota_tools/garage-sign/lib/* ${D}${libdir}
    fi

    # resource control
    install -d ${D}/${systemd_system_unitdir}/aktualizr.service.d
    install -m 0644 ${WORKDIR}/10-resource-control.conf ${D}/${systemd_system_unitdir}/aktualizr.service.d

    sed -i -e 's|@CPU_WEIGHT@|${RESOURCE_CPU_WEIGHT}|g' \
           -e 's|@MEMORY_HIGH@|${RESOURCE_MEMORY_HIGH}|g' \
           -e 's|@MEMORY_MAX@|${RESOURCE_MEMORY_MAX}|g' \
           ${D}${systemd_system_unitdir}/aktualizr.service.d/10-resource-control.conf
}

PACKAGESPLITFUNCS_prepend = "split_hosttools_packages "

python split_hosttools_packages () {
    bindir = d.getVar('bindir')

    # Split all binaries to their own packages except aktualizr-info,
    # aktualizr-info should stay in main package aktualizr.
    do_split_packages(d, bindir, r'^((?!(aktualizr-info)).*)$', '%s', 'Aktualizr tool - %s', extra_depends='aktualizr-configs', prepend=False)
}

PACKAGES_DYNAMIC = "^aktualizr-.* ^garage-.*"

PACKAGES =+ "${PN}-resource-control ${PN}-examples ${PN}-secondary ${PN}-configs ${PN}-host-tools"

ALLOW_EMPTY_${PN}-host-tools = "1"

FILES_${PN} = " \
                ${bindir}/aktualizr \
                ${bindir}/aktualizr-info \
                ${systemd_unitdir}/system/aktualizr.service \
                "

FILES_${PN}-resource-control = " \
                ${systemd_system_unitdir}/aktualizr.service.d/10-resource-control.conf \
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
                ${libdir}/sota/sota-secondary.toml \
                ${systemd_unitdir}/system/aktualizr-secondary.service \
                "

BBCLASSEXTEND = "native"

# vim:set ts=4 sw=4 sts=4 expandtab:
