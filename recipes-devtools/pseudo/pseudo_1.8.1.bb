require recipes-devtools/pseudo/pseudo.inc

SRC_URI = " \
    http://downloads.yoctoproject.org/releases/pseudo/${BPN}-${PV}.tar.bz2 \
    file://fallback-passwd \
    file://fallback-group \
    file://0001-Add-check-for-existence-of-old-file-to-renameat.patch \
"

SRC_URI[md5sum] = "ee38e4fb62ff88ad067b1a5a3825bac7"
SRC_URI[sha256sum] = "dac4ad2d21228053151121320f629d41dd5c0c87695ac4e7aea286c414192ab5"

PSEUDO_EXTRA_OPTS ?= "--enable-force-async --without-passwd-fallback"

do_install_append_class-native () {
	install -d ${D}${sysconfdir}
	# The fallback files should never be modified
	install -m 444 ${WORKDIR}/fallback-passwd ${D}${sysconfdir}/passwd
	install -m 444 ${WORKDIR}/fallback-group ${D}${sysconfdir}/group
}
