
do_install () {
    install -D -p -m0755 ${S}/out/xtest/xtest ${D}${bindir}/xtest

    # install path should match the value set in optee-client/tee-supplicant
    # default TEEC_LOAD_PATH is /lib
    mkdir -p ${D}/${libdir}/optee_armtz/
    install -D -p -m0444 ${S}/out/ta/*/*.ta ${D}/${libdir}/optee_armtz/
}

FILES_${PN} += "${libdir}/optee_armtz/"
