DESCRIPTION = "Boot script template for OTA-enabled image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit deploy

SRC_URI = "file://uEnv-fileenv.txt \
           file://uEnv-fit.txt \
           file://uEnv-rollback.txt \
           file://uEnv.txt"

python () {
  d.setVar('SOTA_BOOTLOADER_EXTRA_PARAMS', d.getVar('SOTA_BOOTLOADER_EXTRA_PARAMS').replace('/', '\/'))
  d.setVar('SOTA_BOOTLOADER_BOOTCOMMAND', d.getVar('SOTA_BOOTLOADER_BOOTCOMMAND').replace('/', '\/'))
  d.setVar('SOTA_BOOTLOADER_KERNEL_ADDR', d.getVar('SOTA_BOOTLOADER_KERNEL_ADDR').replace('/', '\/'))
  d.setVar('SOTA_BOOTLOADER_RAMDISK_ADDR', d.getVar('SOTA_BOOTLOADER_RAMDISK_ADDR').replace('/', '\/'))
  d.setVar('SOTA_BOOTLOADER_FDT_ADDR', d.getVar('SOTA_BOOTLOADER_FDT_ADDR').replace('/', '\/'))
  d.setVar('SOTA_BOOTLOADER_BOOT_PART', d.getVar('SOTA_BOOTLOADER_BOOT_PART').replace('/', '\/'))
  d.setVar('SOTA_BOOTLOADER_MAIN_PART', d.getVar('SOTA_BOOTLOADER_MAIN_PART').replace('/', '\/'))
  d.setVar('SOTA_BOOTLOADER_ROOT_DEVICE', d.getVar('SOTA_BOOTLOADER_ROOT_DEVICE').replace('/', '\/'))
}

do_compile() {

  UENV_TEMPLATE="${WORKDIR}/uEnv.template"
  # Initialization portion
  sed -n '0,/@@INITFINISHED@@/p' < ${WORKDIR}/uEnv.txt | head -n -1 > $UENV_TEMPLATE

  # Support for FIT images
  if [ "${KERNEL_IMAGETYPE}" = "fitImage" ]; then
    cat ${WORKDIR}/uEnv-fit.txt >> $UENV_TEMPLATE
  fi

  # Rollback support
  if [ -n "${SOTA_ROLLBACK_MECHANISM}" ]; then
    cat ${WORKDIR}/uEnv-rollback.txt >> $UENV_TEMPLATE

    if [ "${SOTA_ROLLBACK_MECHANISM}" = "file-env" ]; then
      cat ${WORKDIR}/uEnv-fileenv.txt >> $UENV_TEMPLATE
    fi
  fi

  # The rest of the base template
  sed -n '/@@INITFINISHED@@/,$p' < ${WORKDIR}/uEnv.txt | sed -n '2,$p' >> $UENV_TEMPLATE

  # Substitute the variables
  sed -e 's/@@SOTA_BOOTLOADER_EXTRA_PARAMS@@/${SOTA_BOOTLOADER_EXTRA_PARAMS}/' \
      -e 's/@@SOTA_BOOTLOADER_BOOTCOMMAND@@/${SOTA_BOOTLOADER_BOOTCOMMAND}/' \
      -e 's/@@SOTA_BOOTLOADER_KERNEL_ADDR@@/${SOTA_BOOTLOADER_KERNEL_ADDR}/' \
      -e 's/@@SOTA_BOOTLOADER_RAMDISK_ADDR@@/${SOTA_BOOTLOADER_RAMDISK_ADDR}/' \
      -e 's/@@SOTA_BOOTLOADER_FDT_ADDR@@/${SOTA_BOOTLOADER_FDT_ADDR}/' \
      -e 's/@@SOTA_BOOTLOADER_BOOT_PART@@/${SOTA_BOOTLOADER_BOOT_PART}/' \
      -e 's/@@SOTA_BOOTLOADER_MAIN_PART@@/${SOTA_BOOTLOADER_MAIN_PART}/' \
      -e 's/@@SOTA_BOOTLOADER_ROOT_DEVICE@@/${SOTA_BOOTLOADER_ROOT_DEVICE}/' \
    "$UENV_TEMPLATE" > ${WORKDIR}/uEnv-final.txt
}

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${WORKDIR}/uEnv-final.txt ${DEPLOYDIR}/uEnv.txt
}

addtask deploy after do_compile before do_build
