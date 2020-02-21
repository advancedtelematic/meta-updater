RPI_USE_U_BOOT_sota = "1"

KERNEL_CLASSES_append_sota = " kernel-fitimage"
KERNEL_IMAGETYPE_sota = "fitImage"
INITRAMFS_FSTYPES = "cpio.gz"
OSTREE_KERNEL = "${KERNEL_IMAGETYPE}-${INITRAMFS_IMAGE}-${MACHINE}-${KERNEL_FIT_LINK_NAME}"

# DTB needs to be relocated to apply overlays
UBOOT_DTB_LOADADDRESS = "0x05000000"
UBOOT_DTBO_LOADADDRESS = "0x06000000"

# Deploy config fragment list to OSTree root fs
IMAGE_INSTALL_append = " fit-conf"

DEV_MATCH_DIRECTIVE_pn-networkd-dhcp-conf = "Driver=smsc95xx lan78xx"
IMAGE_INSTALL_append_sota = " virtual/network-configuration "

PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot"
UBOOT_ENTRYPOINT_sota ?= "0x00080000"

IMAGE_FSTYPES_remove_sota = "rpi-sdimg"
OSTREE_BOOTLOADER ?= "u-boot"

def make_dtb_boot_files(d):
    # Generate IMAGE_BOOT_FILES entries for device tree files listed in
    # KERNEL_DEVICETREE.
    #
    # This function was taken from conf/machine/include/rpi-base.inc in
    # meta-raspberrypi
    alldtbs = d.getVar('KERNEL_DEVICETREE')
    imgtyp = d.getVar('KERNEL_IMAGETYPE')

    def transform(dtb):
        base = os.path.basename(dtb)
        if dtb.endswith('dtb'):
            return base
        elif dtb.endswith('dtbo'):
            return '{};{}'.format(base, dtb)

    return ' '.join([transform(dtb) for dtb in alldtbs.split(' ') if dtb])

IMAGE_BOOT_FILES_sota = "bcm2835-bootfiles/* \
                         u-boot.bin;${SDIMG_KERNELIMAGE} \
                         "

# OSTree puts its own boot.scr to bcm2835-bootfiles
# rpi4 and recent rpi3 firmwares needs dtb in /boot partition
# so that they can be read by the firmware
IMAGE_BOOT_FILES_append_sota = "${@make_dtb_boot_files(d)}"

# Just the overlays that will be used should be listed
KERNEL_DEVICETREE_raspberrypi2_sota ?= " bcm2709-rpi-2-b.dtb "
KERNEL_DEVICETREE_raspberrypi3_sota ?= " bcm2710-rpi-3-b.dtb overlays/vc4-kms-v3d.dtbo overlays/rpi-ft5406.dtbo"
KERNEL_DEVICETREE_raspberrypi3-64_sota ?= " broadcom/bcm2710-rpi-3-b.dtb overlays/vc4-kms-v3d.dtbo overlays/vc4-fkms-v3d.dtbo overlays/rpi-ft5406.dtbo"
KERNEL_DEVICETREE_raspberrypi4_sota ?= " bcm2711-rpi-4-b.dtb overlays/vc4-fkms-v3d.dtbo overlays/uart0-rpi4.dtbo"
KERNEL_DEVICETREE_raspberrypi4-64_sota ?= " broadcom/bcm2711-rpi-4-b.dtb overlays/vc4-fkms-v3d.dtbo overlays/uart0-rpi4.dtbo"

SOTA_MAIN_DTB_raspberrypi2 ?= "bcm2709-rpi-2-b.dtb"
SOTA_MAIN_DTB_raspberrypi3 ?= "bcm2710-rpi-3-b.dtb"
SOTA_MAIN_DTB_raspberrypi3-64 ?= "broadcom_bcm2710-rpi-3-b.dtb"
SOTA_MAIN_DTB_raspberrypi4_sota ?= "bcm2711-rpi-4-b.dtb"
SOTA_MAIN_DTB_raspberrypi4-64_sota ?= "broadcom_bcm2711-rpi-4-b.dtb"

SOTA_DT_OVERLAYS_raspberrypi3 ?= "vc4-kms-v3d.dtbo rpi-ft5406.dtbo"
SOTA_DT_OVERLAYS_raspberrypi3-64 ?= "vc4-kms-v3d.dtbo vc4-fkms-v3d.dtbo rpi-ft5406.dtbo"
SOTA_DT_OVERLAYS_raspberrypi4 ?= "vc4-fkms-v3d.dtbo uart0-rpi4.dtbo"
SOTA_DT_OVERLAYS_raspberrypi4-64 ?= "vc4-fkms-v3d.dtbo uart0-rpi4.dtbo"

# Kernel args normally provided by RPi's internal bootloader. Non-updateable
OSTREE_KERNEL_ARGS_sota ?= " 8250.nr_uarts=1 bcm2708_fb.fbwidth=656 bcm2708_fb.fbheight=614 bcm2708_fb.fbswap=1 vc_mem.mem_base=0x3ec00000 vc_mem.mem_size=0x40000000 dwc_otg.lpm_enable=0 console=ttyS0,115200 usbhid.mousepoll=0 "

SOTA_CLIENT_FEATURES_append = " ubootenv"
