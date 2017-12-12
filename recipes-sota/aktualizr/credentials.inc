SRC_URI_append = "${@'file://${SOTA_PACKED_CREDENTIALS}' if d.getVar('SOTA_PACKED_CREDENTIALS', True) else ' '}"
