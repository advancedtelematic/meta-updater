# From meta-linaro
# http://git.linaro.org/openembedded/meta-linaro.git

DESCRIPTION = "A small tool to provide detailed information on the hardware \
configuration of the machine. It can report exact memory configuration, \
firmware version, mainboard configuration, CPU version and speed, cache \
configuration, bus speed, etc. on DMI-capable or EFI systems."
SUMMARY = "Hardware lister"
HOMEPAGE = "http://ezix.org/project/wiki/HardwareLiSter"
SECTION = "console/tools"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=94d55d512a9ba36caa9b7df079bae19f"
DEPENDS = "pciutils \
    usbutils"
COMPATIBLE_HOST = "(i.86|x86_64|arm|aarch64).*-linux"

SRC_URI="http://ezix.org/software/files/lshw-B.${PV}.tar.gz \
    file://cross-compile.patch \
    file://ldflags.patch \
    "

SRC_URI[md5sum] = "67479167add605e8f001097c30e96d0d"
SRC_URI[sha256sum] = "809882429555b93259785cc261dbff04c16c93d064db5f445a51945bc47157cb"

S="${WORKDIR}/lshw-B.${PV}"

do_compile() {
    # build core only - don't ship gui
    oe_runmake -C src core
}

do_install() {
    oe_runmake install DESTDIR=${D}
    # data files provided by dependencies
    rm -rf ${D}/usr/share/lshw
}
