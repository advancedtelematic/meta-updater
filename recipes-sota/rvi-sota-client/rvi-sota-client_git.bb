require rvi-sota-client.inc


SYSTEMD_SERVICE_${PN} = "sota-client.service sota-client-autoprovision.service"

FILES_${PN} = " \
/lib64 \
${bindir}/sota_client \
${bindir}/sota_sysinfo.sh \
${bindir}/sota_provision.sh \
${sysconfdir}/sota_client.version \
${sysconfdir}/sota_certificates \
${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota-client.service', '', d)} \
${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota-client-autoprovision.service', '', d)} \
"

DEPENDS += " openssl openssl-native dbus "
RDEPENDS_${PN} = " \
bash \
curl \
libcrypto \
libssl \
lshw \
jq \
python-petname \
sota-launcher \
zip \
"

export SOTA_PACKED_CREDENTIALS

do_compile_prepend() {
  export SOTA_VERSION=$(make sota-version)
  cd sota-client
}

do_install() {
  ln -fs /lib ${D}/lib64

  install -d ${D}${bindir}
  install -d ${D}${sysconfdir}

  echo `git log -1 --pretty=format:%H` > ${D}${sysconfdir}/sota_client.version
  install -c ${S}/sota-client/docker/sota_certificates ${D}${sysconfdir}

  install -m 0755 target/${TARGET_SYS}/release/sota_client ${D}${bindir}
  install -m 0755 ${S}/sota-client/docker/sota_provision.sh ${D}${bindir}
  install -m 0755 ${S}/sota-client/docker/sota_sysinfo.sh ${D}${bindir}

  if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
    install -d ${D}/${systemd_unitdir}/system
    if [ -n "$SOTA_PACKED_CREDENTIALS" ]; then
      install -m 0644 ${WORKDIR}/sota-client-uptane.service ${D}/${systemd_unitdir}/system/sota-client.service
    else
      install -m 0644 ${WORKDIR}/sota-client-ostree.service ${D}/${systemd_unitdir}/system/sota-client.service
    fi
    install -m 0644 ${WORKDIR}/sota-client-autoprovision.service ${D}/${systemd_unitdir}/system/sota-client-autoprovision.service
  fi
}
