SUMMARY = "Initramfs for booting into libostree managed system"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://init.sh"

S = "${WORKDIR}"

PV = "4"

do_install() {
	install -dm 0755 ${D}/etc
	touch ${D}/etc/initrd-release
	install -dm 0755 ${D}/dev
	install -m 0755 ${WORKDIR}/init.sh ${D}/init
}

inherit allarch

FILES:${PN} += " /dev /etc/initrd-release /init "
