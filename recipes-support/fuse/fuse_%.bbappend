BBCLASSEXTEND_append_sota = " native"

PACKAGES_append_class-native_sota = "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ' fuse-utils-dbg-native fuse-utils-native libulockmgr-native libulockmgr-dev-native libulockmgr-dbg-native', ' ', d)}"
