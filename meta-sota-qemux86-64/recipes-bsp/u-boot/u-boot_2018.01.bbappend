FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

DEPENDS += "swig-native"

SRC_URI +=" \
            file://0001-Set-up-environment-for-OSTree-integration.patch \
          "


do_compile_prepend() {
  export BUILD_ROM=y
}
UBOOT_SUFFIX = "rom"
