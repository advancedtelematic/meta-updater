SRC_URI_append = "${@('file://' + d.getVar('SOTA_PACKED_CREDENTIALS')) if d.getVar('SOTA_PACKED_CREDENTIALS') else ''}"
