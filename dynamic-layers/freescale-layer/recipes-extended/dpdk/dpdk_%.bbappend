
do_install() {
    unset LDFLAGS TARGET_LDFLAGS BUILD_LDFLAGS

    oe_runmake EXTRA_LDFLAGS="-L${STAGING_LIBDIR} --hash-style=gnu"  WERROR_FLAGS="-w" V=1  T="${RTE_TARGET}" DESTDIR="${D}" install CONFIG_RTE_EAL_IGB_UIO=n CONFIG_RTE_KNI_KMOD=y CONFIG_RTE_LIBRTE_PMD_OPENSSL=y

    # Build and install the DPDK examples
    for APP in examples/l2fwd examples/l3fwd  examples/l2fwd-qdma examples/l2fwd-crypto examples/ipsec-secgw examples/kni examples/ip_fragmentation examples/ip_reassembly; do
        temp=`basename ${APP}`
        if [ ${temp} = "ipsec-secgw" ] || [ ${temp} = "l2fwd-crypto" ]; then
            oe_runmake EXTRA_LDFLAGS="-L${STAGING_LIBDIR} --hash-style=gnu"  -C ${APP} CONFIG_RTE_LIBRTE_PMD_OPENSSL=y
        else
            oe_runmake EXTRA_LDFLAGS="-L${STAGING_LIBDIR} --hash-style=gnu" EXTRA_CFLAGS="--sysroot=${STAGING_DIR_HOST} -I${STAGING_INCDIR}" -C ${APP}
        fi

        [ ! -d ${D}/${bindir}/dpdk-example ] && install -d 0644 ${D}/${bindir}/dpdk-example
        install -m 0755 ${S}/examples/`basename ${APP}`/build/`basename ${APP}` \
            ${D}/${bindir}/dpdk-example/
    done
    oe_runmake EXTRA_LDFLAGS="-L${STAGING_LIBDIR} --hash-style=gnu"  -C examples/vhost
    install -m 0755 ${S}/examples/vhost/build/vhost-switch ${D}/${bindir}/dpdk-example/
    oe_runmake EXTRA_LDFLAGS="-L${STAGING_LIBDIR} --hash-style=gnu"  -C examples/cmdif

    install -d 0644 ${D}/usr/share/dpdk/cmdif/include
    install -d 0644 ${D}/usr/share/dpdk/cmdif/lib
    cp examples/cmdif/lib/client/fsl_cmdif_client.h examples/cmdif/lib/server/fsl_cmdif_server.h \
        examples/cmdif/lib/shbp/fsl_shbp.h      ${D}/usr/share/dpdk/cmdif/include
    cp examples/cmdif/lib/${RTE_TARGET}/librte_cmdif.a ${D}/usr/share/dpdk/cmdif/lib

    install -m 0755 ${S}/${RTE_TARGET}/app/testpmd ${D}/${bindir}/dpdk-example/
    rm -fr ${D}/lib
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/dpdk
    install -m 0755 ${S}/${RTE_TARGET}/kmod/rte_kni.ko ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/dpdk
    install -d ${D}/${bindir}/dpdk-example/extras
    cp -rf  ${S}/nxp/* ${D}/${bindir}/dpdk-example/extras/
    rm ${D}/${datadir}/${RTE_TARGET}/app/dpdk-pmdinfogen

    chown root:root -R ${D}
}
