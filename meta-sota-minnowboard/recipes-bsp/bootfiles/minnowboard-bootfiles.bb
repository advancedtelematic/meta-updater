DESCRIPTION = "Boot files (bootscripts etc.) for Minnowboard Max/Turbot"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit deploy

COMPATIBLE_MACHINE = "intel-corei7-64"

S = "${WORKDIR}"

SRC_URI_append_sota = "file://uEnv-ota.txt"

do_deploy() {
    install -d ${DEPLOYDIR}/${PN}
}

do_deploy_append_sota() {
    install -m 0755 ${WORKDIR}/uEnv-ota.txt ${DEPLOYDIR}/${PN}/uEnv.txt
}

addtask deploy before do_package after do_install
do_deploy[dirs] += "${DEPLOYDIR}/${PN}"

PACKAGE_ARCH = "${MACHINE_ARCH}"

