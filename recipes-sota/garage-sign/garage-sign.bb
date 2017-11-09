SUMMARY = "garage-sign"
DESCRIPTION = "Metadata signing tool for ATS Garage"
HOMEPAGE = "https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/index.html"
SECTION = "base"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = "file://${S}/docs/LICENSE;md5=3025e77db7bd3f1d616b3ffd11d54c94"
DEPENDS = ""

PV = "0.2.0-29-gf6f095a"

SRC_URI = " \
  https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/cli-${PV}.tgz \
  "

SRC_URI[md5sum] = "49ee4389570992f0cebb16d5943e4405"
SRC_URI[sha256sum] = "59f902e6507adec3176bdf470fe5dea31996810a6300bd61583638d4ffe37ab3"

S = "${WORKDIR}/${BPN}"

BBCLASSEXTEND =+ "native"

do_install() {
    install -d ${D}${bindir}
    install -m "0755" -t ${D}${bindir} ${S}/bin/*
    install -d ${D}${libdir}
    install -m "0644" -t ${D}${libdir} ${S}/lib/*
}

FILES_${PN} = " \
  /usr/bin \
  /usr/bin/garage-sign.bat \
  /usr/bin/garage-sign \
  /usr/lib/* \
  "
