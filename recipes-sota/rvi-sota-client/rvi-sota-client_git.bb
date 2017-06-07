DESCRIPTION = "sota-client rust recipe"
HOMEPAGE = "https://github.com/advancedtelematic/rvi_sota_client"

LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=65d26fcc2f35ea6a181ac777e42db1ea"

inherit cargo systemd

S = "${WORKDIR}/git"

# When changing this, don't forget to:
# 1) Update PV
# 2) Check that Cargo.lock hasn't changed with git diff old..new Cargo.lock
SRCREV = "0d092c218c823fe38e59e7ecb4589c3770dc6448"

# Generate with:
#   git describe --tags | cut -b2-
# or from the rvi_sota_client repo:
#   make package-version
PV = "0.2.32-186-g313ba1a"

BBCLASSEXTEND = "native"

FILES_${PN} = " \
                /lib64 \
                ${bindir}/canonical_json.py \
                ${bindir}/sota_client \
                ${bindir}/sota_sysinfo.sh \
                ${bindir}/system_info.sh \
                ${bindir}/sota_prov.sh \
                ${sysconfdir}/sota_client.version \
                ${sysconfdir}/sota_certificates \
                ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota_client_autoprovision.service', '', d)} \
                ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota_client.service', '', d)} \
              "

# list of dependencies can be generated from Cargo.lock by running
#   cat Cargo.lock | sed -e '1,/metadata/ d' Cargo.lock | awk '{print "crate://crates.io/"$2 "/" $3" \\"}'
SRC_URI = " \
crate://crates.io/aho-corasick/0.6.3 \
crate://crates.io/backtrace/0.3.0 \
crate://crates.io/backtrace-sys/0.1.10 \
crate://crates.io/base64/0.4.2 \
crate://crates.io/base64/0.5.2 \
crate://crates.io/bit-set/0.4.0 \
crate://crates.io/bit-vec/0.4.3 \
crate://crates.io/bitflags/0.8.2 \
crate://crates.io/byteorder/1.0.0 \
crate://crates.io/bytes/0.4.3 \
crate://crates.io/cfg-if/0.1.0 \
crate://crates.io/chan/0.1.19 \
crate://crates.io/chan-signal/0.2.0 \
crate://crates.io/chrono/0.3.1 \
crate://crates.io/crossbeam/0.2.10 \
crate://crates.io/dbghelp-sys/0.2.0 \
crate://crates.io/dbus/0.5.2 \
crate://crates.io/dtoa/0.4.1 \
crate://crates.io/env_logger/0.4.2 \
crate://crates.io/error-chain/0.10.0 \
crate://crates.io/error-chain/0.7.2 \
crate://crates.io/filetime/0.1.10 \
crate://crates.io/foreign-types/0.2.0 \
crate://crates.io/gcc/0.3.45 \
crate://crates.io/gdi32-sys/0.2.0 \
crate://crates.io/getopts/0.2.14 \
crate://crates.io/hex/0.2.0 \
crate://crates.io/httparse/1.2.2 \
crate://crates.io/hyper/0.10.9 \
crate://crates.io/idna/0.1.1 \
crate://crates.io/iovec/0.1.0 \
crate://crates.io/itoa/0.3.1 \
crate://crates.io/kernel32-sys/0.2.2 \
crate://crates.io/language-tags/0.2.2 \
crate://crates.io/lazy_static/0.2.8 \
crate://crates.io/libc/0.2.22 \
crate://crates.io/log/0.3.7 \
crate://crates.io/matches/0.1.4 \
crate://crates.io/memchr/1.0.1 \
crate://crates.io/metadeps/1.1.1 \
crate://crates.io/mime/0.2.3 \
crate://crates.io/num/0.1.37 \
crate://crates.io/num-integer/0.1.34 \
crate://crates.io/num-iter/0.1.33 \
crate://crates.io/num-traits/0.1.37 \
crate://crates.io/num_cpus/1.4.0 \
crate://crates.io/openssl/0.9.11 \
crate://crates.io/openssl-sys/0.9.11 \
crate://crates.io/pem/0.4.0 \
crate://crates.io/pkg-config/0.3.9 \
crate://crates.io/quote/0.3.15 \
crate://crates.io/rand/0.3.15 \
crate://crates.io/redox_syscall/0.1.17 \
crate://crates.io/regex/0.2.1 \
crate://crates.io/regex-syntax/0.4.0 \
crate://crates.io/ring/0.7.1 \
crate://crates.io/rust-crypto/0.2.36 \
crate://crates.io/rustc-demangle/0.1.4 \
crate://crates.io/rustc-serialize/0.3.24 \
crate://crates.io/rustc_version/0.1.7 \
crate://crates.io/semver/0.1.20 \
crate://crates.io/serde/1.0.2 \
crate://crates.io/serde_derive/1.0.2 \
crate://crates.io/serde_derive_internals/0.15.0 \
crate://crates.io/serde_json/1.0.1 \
crate://crates.io/sha1/0.2.0 \
crate://crates.io/syn/0.11.11 \
crate://crates.io/synom/0.11.3 \
crate://crates.io/tar/0.4.11 \
crate://crates.io/thread-id/3.0.0 \
crate://crates.io/thread_local/0.3.3 \
crate://crates.io/time/0.1.37 \
crate://crates.io/toml/0.2.1 \
crate://crates.io/toml/0.4.0 \
crate://crates.io/traitobject/0.1.0 \
crate://crates.io/tungstenite/0.2.2 \
crate://crates.io/typeable/0.1.2 \
crate://crates.io/unicase/1.4.0 \
crate://crates.io/unicode-bidi/0.2.5 \
crate://crates.io/unicode-normalization/0.1.4 \
crate://crates.io/unicode-xid/0.0.4 \
crate://crates.io/unix_socket/0.5.0 \
crate://crates.io/unreachable/0.1.1 \
crate://crates.io/untrusted/0.3.2 \
crate://crates.io/url/1.4.0 \
crate://crates.io/user32-sys/0.2.0 \
crate://crates.io/utf-8/0.7.0 \
crate://crates.io/utf8-ranges/1.0.0 \
crate://crates.io/uuid/0.5.0 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/winapi-build/0.1.1 \
crate://crates.io/xattr/0.1.11 \
git://github.com/advancedtelematic/rvi_sota_client \
"

SRC_URI[index.md5sum] = "6a635e8a081b4d4ba4cebffd721c2d7d"
SRC_URI[index.sha256sum] = "1913c41d4b8de89a931b6f9e418f83e70a083e12e6c247e8510ee932571ebae2"

SYSTEMD_SERVICE_${PN} = "sota_client.service sota_client_autoprovision.service"

DEPENDS += " openssl openssl-native dbus "
RDEPENDS_${PN} = " libcrypto \
                   libssl \
                   bash \
                   lshw \
                   jq \
                   curl \
                   python \
                   python-canonicaljson \
                   python-json \
                   python-petname \
                   "

export SOTA_PACKED_CREDENTIALS
export SOTA_AUTOPROVISION_CREDENTIALS
export SOTA_AUTOPROVISION_URL

do_compile_prepend() {
  export SOTA_VERSION=$(make sota-version)
}

do_install() {
  install -d ${D}${bindir}
  install -m 0755 target/${TARGET_SYS}/release/sota_client ${D}${bindir}
  install -m 0755 ${S}/run/sota_sysinfo.sh ${D}${bindir}
  ln -fs ${bindir}/sota_sysinfo.sh ${D}${bindir}/system_info.sh  # For compatibilty with old sota.toml files
  install -m 0755 ${S}/run/sota_prov.sh ${D}${bindir}
  install -m 0755 ${S}/run/canonical_json.py ${D}${bindir}

  if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
    install -d ${D}/${systemd_unitdir}/system
    if [ -n "$SOTA_AUTOPROVISION_CREDENTIALS" -o -n "$SOTA_PACKED_CREDENTIALS" ]; then
      install -c ${S}/run/sota_client_uptane_auto.service ${D}${systemd_unitdir}/system/sota_client.service
    else
      install -c ${S}/run/sota_client_ostree.service ${D}${systemd_unitdir}/system/sota_client.service
    fi
    install -c ${S}/run/sota_client_autoprovision.service ${D}${systemd_unitdir}/system/sota_client_autoprovision.service
  fi

  install -d ${D}${sysconfdir}
  echo `git log -1 --pretty=format:%H` > ${D}${sysconfdir}/sota_client.version
  install -c ${S}/run/sota_certificates ${D}${sysconfdir}
  ln -fs /lib ${D}/lib64

}
