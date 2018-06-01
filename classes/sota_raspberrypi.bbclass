RPI_USE_U_BOOT_sota = "1"

KERNEL_CLASSES_append_sota = " kernel-fitimage"
KERNEL_IMAGETYPE_sota = "fitImage"

PREFERRED_PROVIDER_virtual/bootloader_sota ?= "u-boot"
UBOOT_ENTRYPOINT_sota ?= "0x00008000"

IMAGE_FSTYPES_remove_sota = "rpi-sdimg"
OSTREE_BOOTLOADER ?= "u-boot"

# OSTree puts its own boot.scr to bcm2835-bootfiles
IMAGE_BOOT_FILES_sota = "bcm2835-bootfiles/* u-boot.bin;${SDIMG_KERNELIMAGE}"

# Just the overlays that will be used should be listed
KERNEL_DEVICETREE_raspberrypi2_sota ?= " bcm2709-rpi-2-b.dtb "
KERNEL_DEVICETREE_raspberrypi3_sota ?= " bcm2710-rpi-3-b.dtb overlays/vc4-kms-v3d.dtbo overlays/rpi-ft5406.dtbo"

# Kernel args normally provided by RPi's internal bootloader. Non-updateable
OSTREE_KERNEL_ARGS_sota ?= " 8250.nr_uarts=1 bcm2708_fb.fbwidth=720 bcm2708_fb.fbheight=480 bcm2708_fb.fbswap=1 vc_mem.mem_base=0x3ec00000 vc_mem.mem_size=0x40000000 dwc_otg.lpm_enable=0 console=ttyS0,115200 usbhid.mousepoll=0 "

SOTA_CLIENT_FEATURES_append = " ubootenv"
