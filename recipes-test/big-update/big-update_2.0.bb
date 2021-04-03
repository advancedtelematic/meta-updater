DESCRIPTION = "Example Package with 12MB of random, seeded content"
LICENSE = "MPL-2.0"

SRC_URI = "file://rand_file.py"

FILES_${PN} = "${libdir}/big-update"

DEPENDS = "coreutils-native"

inherit python3native

do_install() {
   install -d ${D}${libdir}/big-update
   python3 ${S}/../rand_file.py ${D}${libdir}/big-update/a-big-file $(numfmt --from=iec 12M)
}
