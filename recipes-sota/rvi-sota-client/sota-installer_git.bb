require rvi-sota-client.inc


SYSTEMD_SERVICE_${PN} = "sota-installer.service"

DEPENDS += " rvi-sota-client "

FILES_${PN} = " \
${bindir}/sota-installer \
${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota-installer.service', '', d)} \
"

do_compile_prepend() {
  cd sota-installer
}

do_install() {
  install -d ${D}${bindir}
  install -m 0755 target/${TARGET_SYS}/release/sota-installer ${D}${bindir}

  if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
    install -d ${D}/${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sota-installer.service ${D}/${systemd_unitdir}/system/sota-installer.service
  fi
}
