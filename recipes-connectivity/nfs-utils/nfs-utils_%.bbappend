FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"

SRC_URI += "file://nfs-home-mount.service"

SYSTEMD_SERVICE_${PN} += "nfs-home-mount.service"

CONFFILES_${PN}-client += "${localstatedir}/local/lib"

do_install_append_sota () {
    install -d ${D}${localstatedir}/local
    cp -aPR ${D}${localstatedir}/lib ${D}${localstatedir}/local

    install -m 0644 ${WORKDIR}/nfs-home-mount.service ${D}${systemd_unitdir}/system/
    sed -i -e 's,@BASE_BINDIR@,${base_bindir},g' \
           -e 's,@LOCALSTATEDIR@,${localstatedir},g' \
           ${D}${systemd_unitdir}/system/nfs-home-mount.service

    sed -i -e '0,/Requires=/{s,Requires=\(.*\),Requires=\1 nfs-home-mount.service,}' \
           -e '0,/After=/{s,After=\(.*\),After=\1 nfs-home-mount.service,}' \
           ${D}${systemd_unitdir}/system/nfs-statd.service

    sed -i -e '0,/Requires=/{s,Requires=\(.*\),Requires=\1 nfs-home-mount.service,}' \
           -e '0,/After=/{s,After=\(.*\),After=\1 nfs-home-mount.service,}' \
           ${D}${systemd_unitdir}/system/nfs-mountd.service
}
