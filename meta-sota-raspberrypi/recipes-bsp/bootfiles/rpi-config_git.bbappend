DISABLE_OVERSCAN = "1"

do_deploy_append() {
    if [ "${ENABLE_CMA}" = "1" ] && [ -n "${CMA_LWM}" ]; then
        sed -i '/#cma_lwm/ c\cma_lwm=${CMA_LWM}' ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    fi

    if [ "${ENABLE_CMA}" = "1" ] && [ -n "${CMA_HWM}" ]; then
        sed -i '/#cma_hwm/ c\cma_hwm=${CMA_HWM}' ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    fi

    echo "avoid_warnings=2" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    echo "mask_gpu_interrupt0=0x400" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    echo "dtoverlay=vc4-kms-v3d-overlay,cma-256" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    echo "dtoverlay=rpi-ft5406-overlay" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    echo "dtparam=audio=on" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
}

do_deploy_append_sota() {
    echo "device_tree_address=0x0c800000" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
}

ENABLE_UART_raspberrypi3 = "1"
