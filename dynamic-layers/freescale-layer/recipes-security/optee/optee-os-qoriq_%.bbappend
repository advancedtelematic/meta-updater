
do_install() {
    #install core on boot directory
    install -d ${D}/${libdir}/firmware/
    if [ ${MACHINE} = ls1012afrwy ]; then
        install -m 644 ${B}/out/arm-plat-ls/core/tee_512mb.bin ${D}/${libdir}/firmware/tee_${MACHINE}_512mb.bin
    fi
    install -m 644 ${B}/out/arm-plat-ls/core/tee.bin ${D}/${libdir}/firmware/tee_${MACHINE}.bin
    #install TA devkit
    install -d ${D}/usr/include/optee/export-user_ta/

    for f in  ${B}/out/arm-plat-ls/export-ta_${OPTEE_ARCH}/* ; do
        cp -aR  $f  ${D}/usr/include/optee/export-user_ta/
    done
}

do_deploy() {
    install -d ${DEPLOYDIR}/optee
    for f in ${D}/${libdir}/firmware/*; do
    cp $f ${DEPLOYDIR}/optee/
    done
}

FILES_${PN} = "${libdir}/firmware/"
