SRC_URI_append = "${@('file://' + d.getVar('SOTA_PACKED_CREDENTIALS', True)) if d.getVar('SOTA_PACKED_CREDENTIALS', True) else ''}"
