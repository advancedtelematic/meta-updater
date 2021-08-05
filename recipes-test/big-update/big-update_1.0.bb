DESCRIPTION = "Example Package with 10MB of random, seeded content"
LICENSE = "MPL-2.0"

SRC_URI = "file://rand_file.py"

FILES:${PN} = "/usr/lib/big-update"

DEPENDS = "coreutils-native"

inherit python3native

do_install() {
   install -d ${D}/usr/lib/big-update
   python ${S}/../rand_file.py ${D}/usr/lib/big-update/a-big-file $(numfmt --from=iec 10M)
}
