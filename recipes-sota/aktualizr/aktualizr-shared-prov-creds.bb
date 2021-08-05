SUMMARY = "Credentials for shared provisioning"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

inherit allarch

DEPENDS = "zip-native"
ALLOW_EMPTY:${PN} = "1"

# If the config file from aktualizr used here is changed, you will need to bump
# the version here because of SIGGEN_EXCLUDE_SAFE_RECIPE_DEPS!
PV = "1.0"
PR = "1"

SRC_URI = ""

require credentials.inc

do_install() {
    if [ -n "${SOTA_PACKED_CREDENTIALS}" ]; then
        install -m 0700 -d ${D}${localstatedir}/sota
        # root.json contains the root metadata for bootstrapping the Uptane metadata verification process.
        # autoprov.url has the URL to the device gateway on the server, which is where we send most of our requests.
        # autoprov_credentials.p12 contains the shared provisioning credentials.
        for var in root.json autoprov.url autoprov_credentials.p12; do
            if unzip -l "${SOTA_PACKED_CREDENTIALS}" $var > /dev/null; then
                unzip "${SOTA_PACKED_CREDENTIALS}" $var -d ${T}
                zip -mj -q ${D}${localstatedir}/sota/sota_provisioning_credentials.zip ${T}/$var
            else
                bbwarn "$var is missing from credentials.zip"
            fi
        done
    fi
}

FILES:${PN} = " \
                ${localstatedir}/sota/sota_provisioning_credentials.zip \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
