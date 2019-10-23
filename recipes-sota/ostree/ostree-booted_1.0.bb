SUMMARY = "Indicate an OSTree boot"
DESCRIPTION =  "Indicate an OSTree boot"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"
SRC_URI = "file://touch-ostree"

inherit allarch update-rc.d

INITSCRIPT_NAME = "touch-ostree"
INITSCRIPT_PARAMS = "start 8 2 3 4 5 . stop 20 0 1 6 ."

do_install() {
	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/touch-ostree ${D}${sysconfdir}/init.d/touch-ostree
}
