BBCLASSEXTEND_append_sota = " native nativesdk"

# Hackery to prevent relocatable_native_pcfiles from crashing
do_install_append_class-native () {
	if [ -d ${D}${libdir}/pkgconfig ]; then
		rmdir ${D}${libdir}/pkgconfig
	fi
}
