require aktualizr_common.inc

DEPENDS = "boost-native glib-2.0-native curl-native openssl-native libarchive-native libsodium-native"

inherit native

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_SOTA_TOOLS=ON -DBUILD_OSTREE=OFF -DAKTUALIZR_VERSION=${PV}"

do_install_append () {
    rm -f ${D}${bindir}/aktualizr
    rm -f ${D}${bindir}/aktualizr_cert_provider
    rm -f ${D}${bindir}/garage-deploy
}

FILES_${PN} = " \
                ${bindir}/aktualizr_implicit_writer \
                ${bindir}/garage-push \
                "
