include recipes-core/images/core-image-minimal.bb

SUMMARY = "A minimal Uptane Secondary image running aktualizr-secondary"

LICENSE = "MIT"


# Remove default aktualizr primary, and the provisioning configuration (which
# RDEPENDS on aktualizr)
IMAGE_INSTALL_remove = " \
                        aktualizr \
                        aktualizr-auto-prov \
                        aktualizr-ca-implicit-prov \
                        aktualizr-hsm-prov \
                        aktualizr-implicit-prov \
                        connman \
                        connman-client \
                        "

IMAGE_INSTALL_append = " \
                        aktualizr-secondary \
                        secondary-network-config \
                        "

# vim:set ts=4 sw=4 sts=4 expandtab:
