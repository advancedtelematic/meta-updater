SUMMARY = "Ostree linux kernel, devicetrees and initramfs packager"
DESCRIPTION = "Ostree linux kernel, devicetrees and initramfs packager"
SECTION = "kernel"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Whilst not a module, this ensures we don't get multilib extended (which would make no sense)
inherit module-base kernel-artifact-names

PACKAGES = "ostree-kernel ostree-initramfs ostree-devicetrees"

ALLOW_EMPTY:ostree-initramfs = "1"
ALLOW_EMPTY:ostree-devicetrees = "1"

FILES:ostree-kernel = "${nonarch_base_libdir}/modules/*/vmlinuz"
FILES:ostree-initramfs = "${nonarch_base_libdir}/modules/*/initramfs.img"
FILES:ostree-devicetrees = "${nonarch_base_libdir}/modules/*/dtb/* \
    ${@'' if oe.types.boolean(d.getVar('OSTREE_MULTI_DEVICETREE_SUPPORT')) else '${nonarch_base_libdir}/modules/*/devicetree'} \
"

PACKAGE_ARCH = "${MACHINE_ARCH}"

KERNEL_BUILD_ROOT = "${nonarch_base_libdir}/modules/"

# There's nothing to do here, except install the artifacts where we can package them
do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
deltask do_populate_sysroot

do_install() {
    kerneldir=${D}${KERNEL_BUILD_ROOT}${KERNEL_VERSION}
    install -d $kerneldir

    cp ${DEPLOY_DIR_IMAGE}/${OSTREE_KERNEL} $kerneldir/vmlinuz

    if [ "${KERNEL_IMAGETYPE}" = "fitImage" ]; then
        if [ -n "${INITRAMFS_IMAGE}" ]; then
            # this is a hack for ostree not to override init= in kernel cmdline -
            # make it think that the initramfs is present (while it is in FIT image)
            touch $kerneldir/initramfs.img
        fi
    else
        if [ -n "${INITRAMFS_IMAGE}" ]; then
            cp ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES} $kerneldir/initramfs.img
        fi

        if [ ${@ oe.types.boolean('${OSTREE_DEPLOY_DEVICETREE}')} = True ] && [ -n "${OSTREE_DEVICETREE}" ]; then
            mkdir -p $kerneldir/dtb
            for dts_file in ${OSTREE_DEVICETREE}; do
                dts_file_basename=$(basename $dts_file)
                cp ${DEPLOY_DIR_IMAGE}/$dts_file_basename $kerneldir/dtb/$dts_file_basename
            done

            if [ ${@ oe.types.boolean('${OSTREE_MULTI_DEVICETREE_SUPPORT}')} = False ]; then
                cp $kerneldir/dtb/$(basename $(echo ${OSTREE_DEVICETREE} | awk '{print $1}')) $kerneldir/devicetree
            fi
        fi
    fi
}
do_install[vardepsexclude] = "KERNEL_VERSION"
INITRAMFS_IMAGE ?= ""
do_install[depends] = "virtual/kernel:do_deploy ${@['${INITRAMFS_IMAGE}:do_image_complete', ''][d.getVar('INITRAMFS_IMAGE') == '']}"

python() {
    if not d.getVar('OSTREE_KERNEL'):
        raise bb.parse.SkipRecipe('OSTREE_KERNEL is not defined, maybe your MACHINE config does not inherit sota.bbclass?')
}
