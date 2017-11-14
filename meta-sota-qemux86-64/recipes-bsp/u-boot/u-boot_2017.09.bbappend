FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

DEPENDS += "swig-native"

SRC_URI +=" \
	    file://0001-Replace-wraps-with-built-in-code-to-remove-dependenc.patch \
	    file://0001-Set-up-environment-for-OSTree-integration.patch \
            file://0001-Move-Cache-As-RAM-memory-from-area-mapped-to-ROM-in-.patch \
	    "


do_compile_prepend() {
  export BUILD_ROM=y
}
UBOOT_SUFFIX = "rom"
