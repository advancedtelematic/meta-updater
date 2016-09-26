# Small hook to call ostree-prepare-root on initrd-switch-root

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe"

S = "${WORKDIR}"

SRC_URI = "file://COPYING.GPL"

DEPENDS += " ostree"

FILES_${PN} += "${systemd_unitdir} \
		${systemd_unitdir}/system \
		${systemd_unitdir}/system/initrd-switch-root.target.wants \
		${systemd_unitdir}/system/initrd-switch-root.target.wants/ostree-prepare-root.service \
		${sysconfdir}/initrd-release \
		"

export OSTREE_INITRAMFS_IMAGE

do_install() {
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -d ${D}${sysconfdir}
		echo "NAME=${OSTREE_INITRAMFS_IMAGE}" > ${D}/${sysconfdir}/initrd-release
		install -d ${D}${systemd_unitdir}
		install -d ${D}${systemd_unitdir}/system

		install -d ${D}${systemd_unitdir}/system/initrd-switch-root.target.wants
		ln -s ${systemd_unitdir}/system/ostree-prepare-root.service  \
			"${D}${systemd_unitdir}/system/initrd-switch-root.target.wants/ostree-prepare-root.service"
	fi
}
