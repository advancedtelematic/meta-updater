require aktualizr_common.inc

DEPENDS = "boost curl jansson openssl libarchive libsodium ostree"
RDEPENDS_${PN} = "lshw"

DEPENDS_append = "${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', ' libp11', '', d)}"
RDEPENDS_${PN}_append = "${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', ' engine-pkcs11', '', d)}"
RDEPENDS_${PN}_append = "${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm-test', ' softhsm softhsm-testtoken', '', d)}"

inherit systemd

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_OSTREE=ON ${@bb.utils.contains('SOTA_CLIENT_FEATURES', 'hsm', '-DBUILD_P11=ON', '', d)} -DAKTUALIZR_VERSION=${PV}"

do_install_append () {
    rm ${D}${bindir}/aktualizr_cert_provider
    rm ${D}${bindir}/aktualizr_implicit_writer
    rm ${D}${bindir}/garage-deploy
}

FILES_${PN} = " \
                ${bindir}/aktualizr \
                "
