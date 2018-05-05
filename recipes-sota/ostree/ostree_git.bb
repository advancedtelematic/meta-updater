SUMMARY = "Tool for managing bootable, immutable, versioned filesystem trees"
HOMEPAGE = "https://ostree.readthedocs.io/en/latest/"
LICENSE = "LGPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=5f30f0716dfdd0d91eb439ebec522ec2"

inherit autotools pkgconfig systemd gobject-introspection

SRC_URI = "gitsm://github.com/ostreedev/ostree.git;branch=master"

SRCREV="854a823e05d6fe8b610c02c2a71eaeb2bf1e98a6"

PV = "v2017.13"
PR = "2"

S = "${WORKDIR}/git"

BBCLASSEXTEND = "native"

DEPENDS += "attr libarchive libcap glib-2.0 gpgme libgsystem fuse e2fsprogs curl xz"
DEPENDS += "${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"
RDEPENDS_${PN} = "bash"

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

SYSTEMD_SERVICE_${PN} = "ostree-prepare-root.service ostree-remount.service"

FILES_${PN} += "${libdir}/ostree/ ${libdir}/ostbuild"

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


FILES_${PN} += " \
    ${@bb.utils.contains('DISTRO_FEATURES','systemd','${libdir}/dracut', '', d)} \
    ${datadir}/gir-1.0 \
    ${datadir}/gir-1.0/OSTree-1.0.gir \
    ${libdir}/girepository-1.0 \
    ${libdir}/girepository-1.0/OSTree-1.0.typelib \
    ${libdir}/tmpfiles.d/ostree-tmpfiles.conf \
    ${datadir}/bash-completion/completions/ostree \
    ${systemd_unitdir}/system-generators/ostree-system-generator \
"

PACKAGES =+ "${PN}-switchroot"

FILES_${PN}-switchroot = "${libdir}/ostree/ostree-prepare-root"
RDEPENDS_${PN}-switchroot = ""
