SUMMARY = "garage-sign"
DESCRIPTION = "Metadata signing tool for ATS Garage"
HOMEPAGE = "https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/index.html"
SECTION = "base"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = "file://${S}/docs/LICENSE;md5=3025e77db7bd3f1d616b3ffd11d54c94"
DEPENDS = ""

PV = "0.2.0-57-g3f86c67"

SRC_URI = " \
  https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/cli-${PV}.tgz \
  "

SRC_URI[md5sum] = "5bbe080c0c3a80928b8856d2076dd49a"
SRC_URI[sha256sum] = "f653d24172ed245a6256b2f341a9b77bddf624cd6bbda574c1a85430e3155394"

S = "${WORKDIR}/${BPN}"

BBCLASSEXTEND =+ "native"

do_install() {
    install -d ${D}${bindir}
    install -m "0755" -t ${D}${bindir} ${S}/bin/*
    install -d ${D}${libdir}
    install -m "0644" -t ${D}${libdir} ${S}/lib/*
}

FILES_${PN} = " \
  ${bindir}/garage-sign.bat \
  ${bindir}/garage-sign \
  ${libdir}/* \
  "
