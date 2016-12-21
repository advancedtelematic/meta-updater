include recipes-bsp/u-boot/u-boot.inc
DEPENDS += "dtc-native intel-fsp-native"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=0507cd7da8e7ad6d6701926ec9b84c95"

# This revision corresponds to the tag "v2015.07"
# We use the revision in order to avoid having to fetch it from the
# repo during parse
SRCREV = "baba2f57e8f4ed3fa67fe213d22da0de5e00f204"

SRC_URI += "file://0002-Replace-wraps-with-built-in-code-to-remove-dependenc.patch \
	    http://firmware.intel.com/sites/default/files/2014-WW42.4-MinnowBoardMax.73-64-bit.bin_Release.zip \
	    "

# Hashes for 2014-WW42.4-MinnowBoardMax.73-64-bit.bin_Release.zip
SRC_URI[md5sum] = "1a4256c64a0d846b81d2adf7ce07cce5"
SRC_URI[sha256sum] = "883b1399b89e8e13033367e911a1e69423dffa9a6c4b5d306fc070d9ed7412b7"

PV = "v2015.07+git${SRCPV}"

EXTRA_OEMAKE_append = " KCFLAGS=-fgnu89-inline BUILD_ROM=y"

UBOOT_SUFFIX = "rom"

do_configure_prepend() {
    make ${UBOOT_MACHINE}
    make tools
    ./tools/ifdtool -x ${WORKDIR}/MNW2MAX1.X64.0073.R02.1409160934.bin
    cp flashregion_0_flashdescriptor.bin ./board/intel/minnowmax/descriptor.bin
    cp flashregion_2_intel_me.bin ./board/intel/minnowmax/me.bin
    cp ${STAGING_DIR_NATIVE}/${datadir}/IntelFsp/BayTrailFSP.fd ./board/intel/minnowmax/fsp.bin
    cp ${STAGING_DIR_NATIVE}/${datadir}/IntelFsp/Vga.dat ./board/intel/minnowmax/vga.bin
}

