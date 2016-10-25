do_deploy_append() {
    echo "device_tree_address=0x0c800000" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
}
