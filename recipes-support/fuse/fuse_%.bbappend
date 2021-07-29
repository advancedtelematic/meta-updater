BBCLASSEXTEND:append:sota = " native"

PACKAGES:append:class-native:sota = "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ' fuse-utils-dbg-native fuse-utils-native libulockmgr-native libulockmgr-dev-native libulockmgr-dbg-native', ' ', d)}"
