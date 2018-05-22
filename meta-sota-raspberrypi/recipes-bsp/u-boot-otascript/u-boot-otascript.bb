DESCRIPTION = "Boot script for launching OTA-enabled images on raspberrypi"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

DEPENDS = "u-boot-mkimage-native"

COMPATIBLE_MACHINE = "raspberrypi"

SRC_URI = "file://boot.scr \
	   file://uEnv.txt"

S = "${WORKDIR}"

inherit deploy

do_deploy() {
    install -d ${DEPLOYDIR}/bcm2835-bootfiles

    mkimage -A arm -O linux -T script -C none -a 0 -e 0 -n "Ostree boot script" -d ${S}/boot.scr ${DEPLOYDIR}/bcm2835-bootfiles/boot.scr
    install -m 0755 ${S}/uEnv.txt ${DEPLOYDIR}/bcm2835-bootfiles/uEnv.txt
}

addtask deploy before do_package after do_install
do_deploy[dirs] += "${DEPLOYDIR}/bcm2835-bootfiles"

PACKAGE_ARCH = "${MACHINE_ARCH}"
