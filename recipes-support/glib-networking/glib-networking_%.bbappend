BBCLASSEXTEND_append_sota = " native nativesdk"

# Hackery to prevent relocatable_native_pcfiles from crashing
do_install_append_class-native () {
        rmdir ${D}${libdir}/pkgconfig
}
