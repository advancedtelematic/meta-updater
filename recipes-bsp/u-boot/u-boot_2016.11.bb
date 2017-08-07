require recipes-bsp/u-boot/u-boot.inc

HOMEPAGE = "http://www.denx.de/wiki/U-Boot/WebHome"
SECTION = "bootloaders"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"
PE = "1"

DEPENDS += "dtc-native"

SRCREV = "5ea3e51fc481613a8dee8c02848d1b42c81ad892"
SRC_URI = "git://git.denx.de/u-boot.git"
S = "${WORKDIR}/git"

PV = "v2016.11+git${SRCPV}"

#This patch is not compliant with u-boot 2016.11
#Version of u-boot from yocto 2.2 Morty is 2016.03 from:
# meta/recipes-bsp/u-boot/u-boot_2016.03.bb
SRC_URI_remove_raspberrypi3 = "file://0003-Include-lowlevel_init.o-for-rpi2.patch"
SRC_URI_remove_raspberrypi2 = "file://0003-Include-lowlevel_init.o-for-rpi2.patch"
