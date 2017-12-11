SUMMARY = "garage-sign"
DESCRIPTION = "Metadata signing tool for ATS Garage"
HOMEPAGE = "https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/index.html"
SECTION = "base"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = "file://${S}/docs/LICENSE;md5=3025e77db7bd3f1d616b3ffd11d54c94"
DEPENDS = ""

PV = "0.2.0-56-gcceb1b3"

SRC_URI = " \
  https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/cli-${PV}.tgz \
  "

SRC_URI[md5sum] = "e9acb2d77b0e41f137428b72fd5c6626"
SRC_URI[sha256sum] = "5c9acea8a20262781b1b3dcc5e72c2e4367ebed6447a877cc8aeda2bc7d2c632"

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
