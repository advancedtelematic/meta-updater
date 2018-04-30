FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"

do_configure_append_sota() {
    # ramblk for inird
    kernel_configure_variable BLK_DEV_RAM y
}

