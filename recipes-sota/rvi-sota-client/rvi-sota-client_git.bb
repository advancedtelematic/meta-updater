DESCRIPTION = "SOTA Reference Implementation project - Client"
HOMEPAGE = "https://github.com/advancedtelematic/rvi_sota_client"
LICENSE = "MPL-2.0"

inherit cargo systemd

SRC_URI = "git://github.com/advancedtelematic/rvi_sota_client.git;protocol=https \
           file://rvi-sota-client.service \
          "
SRCREV="825be11b03f89c52e5441b3d26e1cbf63fd313dd"
LIC_FILES_CHKSUM="file://LICENSE;md5=65d26fcc2f35ea6a181ac777e42db1ea"

S = "${WORKDIR}/git"

BBCLASSEXTEND = "native"

DEPENDS += "dbus openssl"
RDEPENDS_${PN} += "dbus-lib libcrypto libssl bash"

SYSTEMD_SERVICE_${PN} = "rvi-sota-client.service"

do_install_append() {
 install -m 0755 -p -D ${S}/client.toml ${D}/var/sota/client.toml
 install -m 0755 -p -D ${S}/docker/run.sh ${D}${bindir}/run.sh
 if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
  install -p -D ${WORKDIR}/rvi-sota-client.service ${D}${systemd_unitdir}/system/rvi-sota-client.service
 fi
}

## dbus-rs
SRC_URI += "\
        git://github.com/diwic/dbus-rs.git;protocol=https;name=dbus-rs;destsuffix=dbus-rs \
        file://dbus-rs/0001-Cast-correctly-c_char-raw-pointers-for-ARM.patch;patchdir=../dbus-rs \
"

# 0.1.2
SRCREV_dbus-rs = "c2c4c98adcf9949992ac5b0050bf17afe10868c9"

SRCREV_FORMAT .= "_dbus-rs"
EXTRA_OECARGO_PATHS += "${WORKDIR}/dbus-rs"

## rust-openssl
SRC_URI += "git://github.com/sfackler/rust-openssl.git;protocol=https;name=rust-openssl;destsuffix=rust-openssl "

# 0.7.10
SRCREV_rust-openssl = "d6bc3bb16f2673f610e9310041fc030ea9b90187"

SRCREV_FORMAT .= "_rust-openssl"
EXTRA_OECARGO_PATHS += "${WORKDIR}/rust-openssl"

## hyper
SRC_URI += "git://github.com/hyperium/hyper.git;protocol=https;name=hyper;destsuffix=hyper "

# 0.9.1
SRCREV_hyper = "4828437551c7f5ed3f54acb1c1bf1fd50a6a3516"

SRCREV_FORMAT .= "_hyper"
EXTRA_OECARGO_PATHS += "${WORKDIR}/hyper"
