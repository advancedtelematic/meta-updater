require aktualizr_common.inc

DEPENDS = "boost-native openssl-native libarchive-native libsodium-native"

inherit native

EXTRA_OECMAKE = "-DWARNING_AS_ERROR=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_OSTREE=OFF -DAKTUALIZR_VERSION=${PV}"

do_install_append () {
    rm ${D}${bindir}/aktualizr
    rm ${D}${bindir}/aktualizr_cert_provider
}

FILES_${PN} = " \
                ${bindir}/aktualizr_implicit_writer \
                "
