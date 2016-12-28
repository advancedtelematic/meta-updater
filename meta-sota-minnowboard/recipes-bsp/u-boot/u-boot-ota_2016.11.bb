include recipes-bsp/u-boot/u-boot.inc
DEPENDS += "dtc-native intel-fsp-native iasl-native"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

# This revision corresponds to the tag "v2016.11"
# We use the revision in order to avoid having to fetch it from the
# repo during parse
SRCREV = "29e0cfb4f77f7aa369136302cee14a91e22dca71"

SRC_URI += "file://0002-Replace-wraps-with-built-in-code-to-remove-dependenc.patch \
	    http://firmware.intel.com/sites/default/files/MinnowBoard.MAX_.X64.92.R01.zip \
	    "

# Hashes for MinnowBoard.MAX_.X64.92.R01.zip
SRC_URI[md5sum] = "236070e3d0fb193e03a102939822cf59"
SRC_URI[sha256sum] = "708f00d835cc9c49df4e937ef59852ccb6b95026291ac9779b5411dd09baed1f"

PV = "v2016.11+git${SRCPV}"

EXTRA_OEMAKE_append = " KCFLAGS=-fgnu89-inline BUILD_ROM=y"

UBOOT_SUFFIX = "rom"

do_configure_prepend() {
    make ${UBOOT_MACHINE}
    make tools
    ./tools/ifdtool -x ${WORKDIR}/MNW2MAX1.X64.0092.R01.1605221712.bin
    cp flashregion_0_flashdescriptor.bin ./board/intel/minnowmax/descriptor.bin
    cp flashregion_2_intel_me.bin ./board/intel/minnowmax/me.bin
    cp ${STAGING_DIR_NATIVE}/${datadir}/IntelFsp/BayTrailFSP.fd ./board/intel/minnowmax/fsp.bin
    cp ${STAGING_DIR_NATIVE}/${datadir}/IntelFsp/Vga.dat ./board/intel/minnowmax/vga.bin
}

