SUMMARY = "Aktualizr configuration for device credential provisioning with HSM support"
DESCRIPTION = "Configuration for provisioning Aktualizr with device credentials using externally provided or generated CA with HSM support"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch

# We need to get the config files from the aktualizr-configs package built by
# the aktualizr (target) recipe.
DEPENDS = "aktualizr"

# If the config file from aktualizr used here is changed, you will need to bump
# the version here because of SIGGEN_EXCLUDE_SAFE_RECIPE_DEPS!
PV = "1.0"
PR = "7"

SRC_URI = ""

do_install() {
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${STAGING_DIR_HOST}${libdir}/sota/sota-device-cred-hsm.toml \
        ${D}${libdir}/sota/conf.d/20-sota-device-cred-hsm.toml
}

FILES:${PN} = " \
                ${libdir}/sota/conf.d \
                ${libdir}/sota/conf.d/20-sota-device-cred-hsm.toml \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
