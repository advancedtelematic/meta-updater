SUMMARY = "garage-sign"
DESCRIPTION = "Metadata signing tool for ATS Garage"
HOMEPAGE = "https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/index.html"
SECTION = "base"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = "file://${S}/docs/LICENSE;md5=3025e77db7bd3f1d616b3ffd11d54c94"
DEPENDS = ""

PV = "0.2.0-6-g6af6ecd"

SRC_URI = " \
  https://ats-tuf-cli-releases.s3-eu-central-1.amazonaws.com/cli-${PV}.tgz \
  "

SRC_URI[md5sum] = "496ee983f03218f3e3f8d4f56c5d8d8d"
SRC_URI[sha256sum] = "1ab296d7abadc40f5ad851ae62a7dff8423bd7167fa9fdc1972c88c5fe90dd40"

S = "${WORKDIR}/${BPN}"

BBCLASSEXTEND =+ "native"

do_install() {
    install -d ${D}${bindir}
    install -m "0755" -t ${D}${bindir} ${S}/bin/*
    install -d ${D}${libdir}
    install -m "0644" -t ${D}${libdir} ${S}/lib/*
}

