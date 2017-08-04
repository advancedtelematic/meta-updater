BBCLASSEXTEND_append_sota = " native"

DEPENDS_append_class-native = "${@bb.utils.contains('DISTRO_FEATURES', 'sota', ' glib-networking-native', ' ', d)}"
