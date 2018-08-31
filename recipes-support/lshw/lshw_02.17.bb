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
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"
DEPENDS = "pciutils \
    usbutils"
COMPATIBLE_HOST = "(i.86|x86_64|arm|aarch64).*-linux"

SRC_URI="http://ezix.org/software/files/lshw-B.${PV}.tar.gz \
    file://cross-compile.patch \
    file://ldflags.patch \
    "

SRC_URI[md5sum] = "a5feb796cb302850eaf5b4530888e3ed"
SRC_URI[sha256sum] = "eb9cc053fa0f1e78685cb695596e73931bfb55d2377e3bc3b8b94aff4c5a489c"

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
