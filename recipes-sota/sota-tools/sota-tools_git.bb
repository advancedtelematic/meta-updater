DESCRIPTION = "Utility to push data to a server"
LICENSE = "MPL-2.0"

LIC_FILES_CHKSUM = "file://LICENSE;md5=65d26fcc2f35ea6a181ac777e42db1ea"

S = "${WORKDIR}/git"

SRC_URI = "gitsm://github.com/advancedtelematic/sota-tools.git;branch=feat/cacert"
SRCREV = "3cf45bc0cc447c7570a7de780ee33b3dd849b10a"

inherit cmake

DEPENDS = "boost glib-2.0 curl"

BBCLASSEXTEND = "native"

FILES_${PN} = "${bindir}/garage-push"

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF"

do_install() {
   install -d ${D}/${bindir}
   install -m 755 garage-push ${D}/${bindir}
}
