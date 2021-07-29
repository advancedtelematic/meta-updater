DESCRIPTION = "Example Package with 12MB of random, seeded content"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

SRC_URI = "file://rand_file.py"

FILES:${PN} = "${libdir}/big-update"

DEPENDS = "coreutils-native"

inherit python3native

do_install() {
   install -d ${D}${libdir}/big-update
   python3 ${S}/../rand_file.py ${D}${libdir}/big-update/a-big-file $(numfmt --from=iec 12M)
}
