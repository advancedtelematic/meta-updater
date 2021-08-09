SUMMARY = "Tool for managing bootable, immutable, versioned filesystem trees"
HOMEPAGE = "https://ostree.readthedocs.io/en/latest/"
LICENSE = "LGPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=5f30f0716dfdd0d91eb439ebec522ec2"

inherit autotools pkgconfig systemd bash-completion gobject-introspection

SRC_URI = "gitsm://github.com/ostreedev/ostree.git;branch=main"

SRCREV = "f3eba6bcec39c163eb831c02c148ffa483292906"

PV = "v2018.9"

S = "${WORKDIR}/git"

BBCLASSEXTEND = "native"

DEPENDS += "attr bison-native libarchive libcap glib-2.0 gpgme fuse e2fsprogs curl xz"
DEPENDS += "${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"
RDEPENDS_${PN}-dracut = "bash"

CFLAGS_append = " -Wno-error=missing-prototypes"
EXTRA_OECONF = "--disable-gtk-doc --disable-man --with-smack --with-builtin-grub2-mkconfig --with-curl --without-soup"
EXTRA_OECONF_append_class-native = " --enable-wrpseudo-compat"

PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"
PACKAGECONFIG[systemd] = "--with-systemdsystemunitdir=${systemd_unitdir}/system/ --with-dracut"

# Path to ${prefix}/lib/ostree/ostree-grub-generator is hardcoded on the
#  do_configure stage so we do depend on it
SYSROOT_DIR = "${STAGING_DIR_TARGET}"
SYSROOT_DIR_class-native = "${STAGING_DIR_NATIVE}"
do_configure[vardeps] += "SYSROOT_DIR"

SYSTEMD_SERVICE_${PN} = "ostree-prepare-root.service ostree-remount.service ostree-finalize-staged.service"

export BUILD_SYS
export HOST_SYS
export STAGING_INCDIR
export STAGING_LIBDIR

do_configure_prepend() {
    unset docdir
    NOCONFIGURE=1 "${S}/autogen.sh"
}

do_install_append_class-native() {
    create_wrapper ${D}${bindir}/ostree OSTREE_GRUB2_EXEC="${STAGING_LIBDIR_NATIVE}/ostree/ostree-grub-generator"
}

PACKAGES += " \
    ${PN}-switchroot \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'ostree-dracut', '', d)} \
"

FILES_${PN} = "${bindir} \
    ${sysconfdir}/ostree \
    ${datadir}/ostree \
    ${libdir}/*.so.* \
    ${libdir}/ostree/ostree-grub-generator \
    ${libdir}/ostree/ostree-remount \
    ${libdir}/girepository-1.0/* \
    ${@bb.utils.contains('DISTRO_FEATURES','systemd','${libdir}/tmpfiles.d', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES','systemd','${systemd_unitdir}/system/*.path', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES','systemd','${systemd_unitdir}/system-generators', '', d)} \
"
FILES_${PN}-dev += " ${datadir}/gir-1.0"
FILES_${PN}-dracut = "${sysconfdir}/dracut.conf.d ${libdir}/dracut"
FILES_${PN}-switchroot = "${libdir}/ostree/ostree-prepare-root"
