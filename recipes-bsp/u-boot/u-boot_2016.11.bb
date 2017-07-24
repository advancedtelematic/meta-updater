require recipes-bsp/u-boot/u-boot.inc

DEPENDS += "dtc-native"

SRCREV = "5ea3e51fc481613a8dee8c02848d1b42c81ad892"

PV = "v2016.11+git${SRCPV}"

#This patch is not compliant with u-boot 2016.11
#Version of u-boot from yocto 2.2 Morty is 2016.03 from:
# meta/recipes-bsp/u-boot/u-boot_2016.03.bb
SRC_URI_remove_raspberrypi3 = "file://0003-Include-lowlevel_init.o-for-rpi2.patch"
SRC_URI_remove_raspberrypi2 = "file://0003-Include-lowlevel_init.o-for-rpi2.patch"
