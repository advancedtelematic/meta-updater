DESCRIPTION = "python-frozendict recipe"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f4da037a49c09b456fdbbc7a5bd36132"

SRCREV = "c5d16bafcca7b72ff3e8f40d3a9081e4c9233f1b"
SRC_URI = "git://github.com/slezica/python-frozendict.git"
S = "${WORKDIR}/git"

PV = "1.2"
inherit setuptools

