DESCRIPTION = "python-petname recipe"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "d0b767cdb1567defb104f29c3fd022239a7f231e"
SRC_URI = "git://github.com/dustinkirkland/python-petname.git"
S = "${WORKDIR}/git"

PV = "2.2"
inherit setuptools
RDEPENDS_${PN} = " python-setuptools \
                   python-argparse \
                   "

FILES_${PN} = "${libdir} ${bindir}/petname"
