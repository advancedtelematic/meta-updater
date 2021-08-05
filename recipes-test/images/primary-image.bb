include recipes-core/images/core-image-minimal.bb

SUMMARY = "A minimal Uptane Primary image running aktualizr, for testing with a Linux secondary"

LICENSE = "MPL-2.0"

IMAGE_INSTALL:remove = " \
			network-configuration \
                        "

IMAGE_INSTALL:append = " \
			 primary-network-config \
			 primary-config \
                       "

# vim:set ts=4 sw=4 sts=4 expandtab:
