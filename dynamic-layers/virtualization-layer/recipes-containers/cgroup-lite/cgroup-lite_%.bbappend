
do_install() {
	install -d ${D}/${bindir}
	install -m 0755 ${S}/scripts/cgroups-mount ${D}/${bindir}
	install -m 0755 ${S}/scripts/cgroups-umount ${D}/${bindir}

	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/cgroups-init ${D}${sysconfdir}/init.d/cgroups-init

	install -d ${D}${systemd_unitdir}/system
	ln -sf /dev/null ${D}${systemd_unitdir}/system/cgroups-init.service
}
