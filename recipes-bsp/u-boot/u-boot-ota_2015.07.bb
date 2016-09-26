include recipes-bsp/u-boot/u-boot.inc
DEPENDS += "dtc-native"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=0507cd7da8e7ad6d6701926ec9b84c95"

# This revision corresponds to the tag "v2015.07"
# We use the revision in order to avoid having to fetch it from the
# repo during parse
SRCREV = "baba2f57e8f4ed3fa67fe213d22da0de5e00f204"

SRC_URI += "file://0001-Set-up-environment-for-OSTree-integration.patch \
	    file://0002-Replace-wraps-with-built-in-code-to-remove-dependenc.patch \
	    "

PV = "v2015.07+git${SRCPV}"

EXTRA_OEMAKE_append = " KCFLAGS=-fgnu89-inline "
EXTRA_OEMAKE_append_qemux86 = " BUILD_ROM=y"
EXTRA_OEMAKE_append_qemux86-64 = " BUILD_ROM=y"

UBOOT_SUFFIX = "bin"
UBOOT_SUFFIX_qemux86 = "rom"
UBOOT_SUFFIX_qemux86-64 = "rom"
