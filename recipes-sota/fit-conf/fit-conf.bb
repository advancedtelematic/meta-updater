SUMMARY = "FIT image configuration for u-boot to use"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PACKAGE_ARCH = "${MACHINE_ARCH}"

do_install() {
	mkdir -p ${D}${libdir}
	echo -n "fit_conf=" >${D}${libdir}/fit_conf

	if [ -n ${SOTA_MAIN_DTB} ]; then
		echo -n "#conf@${SOTA_MAIN_DTB}" >> ${D}${libdir}/fit_conf
	fi

	for ovrl in ${SOTA_DT_OVERLAYS}; do
		echo -n "#conf@overlays_${ovrl}" >> ${D}${libdir}/fit_conf
	done

	for conf_frag in ${SOTA_EXTRA_CONF_FRAGS}; do
		echo -n "#${conf_frag}" >> ${D}${libdir}/fit_conf
	done
}

FILES:${PN} += "${libdir}/fit_conf"
