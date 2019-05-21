include recipes-core/images/core-image-minimal.bb

SUMMARY = "A minimal Uptane Secondary image running aktualizr-secondary"

LICENSE = "MPL-2.0"

SECONDARY_SERIAL_ID ?= ""
SOTA_HARDWARE_ID ?= "${MACHINE}-sndry"

# Remove default aktualizr primary, and the provisioning configuration (which
# RDEPENDS on aktualizr)
IMAGE_INSTALL_remove = " \
                        aktualizr \
                        aktualizr-auto-prov \
                        aktualizr-auto-prov-creds \
                        aktualizr-ca-implicit-prov \
                        aktualizr-ca-implicit-prov-creds \
                        aktualizr-hsm-prov \
                        aktualizr-uboot-env-rollback \
                        virtual/network-configuration \
                        "

IMAGE_INSTALL_append = " \
                        aktualizr-secondary \
                        secondary-network-config \
                        secondary-config \
                        "

# vim:set ts=4 sw=4 sts=4 expandtab:
