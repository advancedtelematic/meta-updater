S = "${WORKDIR}/git"

LICENSE = "IntelFSPRULAC"
LIC_FILES_CHKSUM = "file://${S}/FspKitProductionRULACLicense.pdf;md5=37802f528bc83ad078606e5932e93e02"

SRC_URI = "git://github.com/IntelFsp/FSP.git;branch=BayTrail"
SRCREV="187409120bcba0ccc3fb514deb3bb923e9723c0c"

inherit native

FILES_${PN} = "${datadir}/IntelFsp/BayTrailFSP.fd \
	       ${datadir}/IntelFsp/Vga.dat"

do_install() {
    install -d ${D}${datadir}/IntelFsp
    install -m 0644 ${S}/BayTrailFspBinPkg/FspBin/BayTrailFSP.fd ${D}${datadir}/IntelFsp/BayTrailFSP.fd
    install -m 0644 ${S}/BayTrailFspBinPkg/Vbios/Vga.dat ${D}${datadir}/IntelFsp/Vga.dat
}
