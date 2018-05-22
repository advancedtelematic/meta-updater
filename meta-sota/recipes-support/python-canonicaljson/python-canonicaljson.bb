DESCRIPTION = "python-canonicaljson recipe"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=2ee41112a44fe7014dce33e26468ba93"

SRCREV = "92e2c06871cc275c2a8b8e3e899141a212aae0e8"
SRC_URI = "git://github.com/matrix-org/python-canonicaljson.git"
S = "${WORKDIR}/git"

# Generate with:
#   git describe --tags | cut -b2-
PV = "1.0.0"
inherit setuptools

RDEPENDS_${PN} = "\
	python-simplejson \
	python-frozendict \
	"
