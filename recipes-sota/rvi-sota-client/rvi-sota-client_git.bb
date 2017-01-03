DESCRIPTION = "sota-client rust recipe"
HOMEPAGE = "https://github.com/advancedtelematic/rvi_sota_client"

LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=65d26fcc2f35ea6a181ac777e42db1ea"

inherit cargo systemd

S = "${WORKDIR}/git"

SRCREV = "fe04468871d3d6b784eabc507ed68cef80b4dea1"

# Generate with:
#   git describe --tags | cut -b2-
PV = "0.2.23-21-gfe04468"

BBCLASSEXTEND = "native"

FILES_${PN} = " \
                ${bindir}/sota_client \
                ${bindir}/sota_sysinfo.sh \
		${bindir}/sota_ostree.sh \
                ${sysconfdir}/sota_client.version \
                ${sysconfdir}/sota_certificates \
                ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota_client.service', '', d)} \
              "

SRC_URI = " \
crate://crates.io/aho-corasick/0.5.3 \
crate://crates.io/bit-set/0.2.0 \
crate://crates.io/bit-vec/0.4.3 \
crate://crates.io/bitflags/0.4.0 \
crate://crates.io/bitflags/0.7.0 \
crate://crates.io/bytes/0.3.0 \
crate://crates.io/cfg-if/0.1.0 \
crate://crates.io/chan-signal/0.1.7 \
crate://crates.io/chan/0.1.18 \
crate://crates.io/cookie/0.2.5 \
crate://crates.io/crossbeam/0.2.10 \
crate://crates.io/dbus/0.4.1 \
crate://crates.io/env_logger/0.3.5 \
crate://crates.io/gcc/0.3.39 \
crate://crates.io/gdi32-sys/0.2.0 \
crate://crates.io/getopts/0.2.14 \
crate://crates.io/hpack/0.2.0 \
crate://crates.io/httparse/1.2.1 \
crate://crates.io/hyper/0.9.13 \
crate://crates.io/idna/0.1.0 \
crate://crates.io/kernel32-sys/0.2.2 \
crate://crates.io/language-tags/0.2.2 \
crate://crates.io/lazy_static/0.1.16 \
crate://crates.io/lazy_static/0.2.2 \
crate://crates.io/libc/0.2.18 \
crate://crates.io/log/0.3.6 \
crate://crates.io/matches/0.1.4 \
crate://crates.io/memchr/0.1.11 \
crate://crates.io/mime/0.2.2 \
crate://crates.io/mio/0.5.1 \
crate://crates.io/miow/0.1.3 \
crate://crates.io/net2/0.2.26 \
crate://crates.io/nix/0.5.1 \
crate://crates.io/nom/1.2.4 \
crate://crates.io/num_cpus/1.2.0 \
crate://crates.io/openssl-sys/0.9.3 \
crate://crates.io/openssl/0.9.3 \
crate://crates.io/pkg-config/0.3.8 \
crate://crates.io/rand/0.3.15 \
crate://crates.io/regex-syntax/0.3.9 \
crate://crates.io/regex/0.1.80 \
crate://crates.io/rust-crypto/0.2.36 \
crate://crates.io/rustc-serialize/0.3.22 \
crate://crates.io/rustc_version/0.1.7 \
crate://crates.io/semver/0.1.20 \
crate://crates.io/sha1/0.2.0 \
crate://crates.io/slab/0.1.3 \
crate://crates.io/solicit/0.4.4 \
crate://crates.io/thread-id/2.0.0 \
crate://crates.io/thread_local/0.2.7 \
crate://crates.io/time/0.1.35 \
crate://crates.io/toml/0.2.1 \
crate://crates.io/traitobject/0.0.1 \
crate://crates.io/typeable/0.1.2 \
crate://crates.io/unicase/1.4.0 \
crate://crates.io/unicode-bidi/0.2.3 \
crate://crates.io/unicode-normalization/0.1.2 \
crate://crates.io/unix_socket/0.5.0 \
crate://crates.io/url/1.2.3 \
crate://crates.io/user32-sys/0.2.0 \
crate://crates.io/utf8-ranges/0.1.3 \
crate://crates.io/winapi-build/0.1.1 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/ws/0.5.3 \
crate://crates.io/ws2_32-sys/0.2.1 \
git://github.com/advancedtelematic/rvi_sota_client \
crate-index://crates.io/213b1a455d9270888c03a42c8d29975369102caa \
"
SRC_URI[index.md5sum] = "79f10f436dbf26737cc80445746f16b4"
SRC_URI[index.sha256sum] = "86114b93f1f51aaf0aec3af0751d214b351f4ff9839ba031315c1b19dcbb1913"

SYSTEMD_SERVICE_${PN} = "sota_client.service"

DEPENDS += " openssl "
RDEPENDS_${PN} = " libcrypto \
                   libssl \
                   dbus \
                   bash \
                   lshw \
                   jq \
                   "

do_compile_prepend() {
  export SOTA_VERSION=$(make sota-version)
}

do_install() {
  install -d ${D}${bindir}
  install -m 0755 target/${TARGET_SYS}/release/sota_client ${D}${bindir}
  install -m 0755 run/sota_sysinfo.sh ${D}${bindir}
  install -m 0755 run/sota_ostree.sh ${D}${bindir}

  if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
    install -d ${D}${systemd_unitdir}/system
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'true', 'false', d)}; then
      install -c ${S}/run/sota_client_ostree.service ${D}${systemd_unitdir}/system/sota_client.service
    else
      install -c ${S}/run/sota_client.service ${D}${systemd_unitdir}/system/sota_client.service
    fi
  fi

  install -d ${D}${sysconfdir}
  echo `git log -1 --pretty=format:%H` > ${D}${sysconfdir}/sota_client.version
  install -c ${S}/run/sota_certificates ${D}${sysconfdir}
}
