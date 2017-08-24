require rvi-sota-client.inc


DEPENDS += " rvi-sota-client "
FILES_${PN} = "${bindir}/sota-launcher"


do_compile_prepend() {
  cd sota-launcher
}

do_install() {
  install -d ${D}${bindir}
  install -m 0755 target/${TARGET_SYS}/release/sota-launcher ${D}${bindir}
}
