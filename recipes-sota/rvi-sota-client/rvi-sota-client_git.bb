DESCRIPTION = "sota-client rust recipe"
HOMEPAGE = "https://github.com/advancedtelematic/rvi_sota_client"

LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=65d26fcc2f35ea6a181ac777e42db1ea"

inherit systemd cargo

S = "${WORKDIR}/git"

# When changing this, don't forget to:
# 1) Update PV
# 2) Check that Cargo.lock hasn't changed with git diff old..new Cargo.lock
SRCREV = "972e2cf46c85335ec68ee98c0eae127c6b491b81"

# Generate with:
#   git describe --tags | cut -b2-
# or from the rvi_sota_client repo:
#   make package-version
PV = "0.2.32-104-g972e2cf"

BBCLASSEXTEND = "native"

FILES_${PN} = " \
                /lib64 \
                ${bindir}/canonical_json.py \
                ${bindir}/sota_client \
                ${bindir}/sota_sysinfo.sh \
                ${bindir}/system_info.sh \
                ${bindir}/sota_ostree.sh \
                ${bindir}/sota_prov.sh \
                ${sysconfdir}/sota_client.version \
                ${sysconfdir}/sota_certificates \
                /var/sota/sota_provisioning_credentials.p12 \
                /var/sota/sota_provisioning_url.env \
                ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota_client_autoprovision.service', '', d)} \
                ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/sota_client.service', '', d)} \
              "

SRC_URI = " \
crate://crates.io/aho-corasick/0.5.3 \
crate://crates.io/bit-set/0.2.0 \
crate://crates.io/bit-vec/0.4.3 \
crate://crates.io/bitflags/0.7.0 \
crate://crates.io/bytes/0.3.0 \
crate://crates.io/cfg-if/0.1.0 \
crate://crates.io/chan-signal/0.1.7 \
crate://crates.io/chan/0.1.19 \
crate://crates.io/chrono/0.3.0 \
crate://crates.io/cookie/0.2.5 \
crate://crates.io/crossbeam/0.2.10 \
crate://crates.io/dbus/0.4.1 \
crate://crates.io/dtoa/0.4.1 \
crate://crates.io/env_logger/0.3.5 \
crate://crates.io/foreign-types/0.2.0 \
crate://crates.io/gcc/0.3.43 \
crate://crates.io/gdi32-sys/0.2.0 \
crate://crates.io/getopts/0.2.14 \
crate://crates.io/hpack/0.2.0 \
crate://crates.io/httparse/1.2.1 \
crate://crates.io/hyper/0.9.18 \
crate://crates.io/idna/0.1.0 \
crate://crates.io/itoa/0.3.1 \
crate://crates.io/kernel32-sys/0.2.2 \
crate://crates.io/language-tags/0.2.2 \
crate://crates.io/lazy_static/0.1.16 \
crate://crates.io/lazy_static/0.2.2 \
crate://crates.io/lazycell/0.4.0 \
crate://crates.io/libc/0.2.20 \
crate://crates.io/log/0.3.6 \
crate://crates.io/matches/0.1.4 \
crate://crates.io/memchr/0.1.11 \
crate://crates.io/mime/0.2.2 \
crate://crates.io/mio/0.6.4 \
crate://crates.io/miow/0.2.0 \
crate://crates.io/net2/0.2.26 \
crate://crates.io/nix/0.7.0 \
crate://crates.io/nom/1.2.4 \
crate://crates.io/num/0.1.36 \
crate://crates.io/num-integer/0.1.32 \
crate://crates.io/num-iter/0.1.32 \
crate://crates.io/num-traits/0.1.36 \
crate://crates.io/num_cpus/1.2.1 \
crate://crates.io/openssl-sys/0.9.7 \
crate://crates.io/openssl/0.9.7 \
crate://crates.io/pem/0.2.0 \
crate://crates.io/pkg-config/0.3.9 \
crate://crates.io/quote/0.3.13 \
crate://crates.io/rand/0.3.15 \
crate://crates.io/ring/0.7.1 \
crate://crates.io/redox_syscall/0.1.16 \
crate://crates.io/regex-syntax/0.3.9 \
crate://crates.io/regex/0.1.80 \
crate://crates.io/rust-crypto/0.2.36 \
crate://crates.io/rustc-serialize/0.3.22 \
crate://crates.io/rustc_version/0.1.7 \
crate://crates.io/semver/0.1.20 \
crate://crates.io/sha1/0.2.0 \
crate://crates.io/slab/0.3.0 \
crate://crates.io/solicit/0.4.4 \
crate://crates.io/time/0.1.36 \
crate://crates.io/thread-id/2.0.0 \
crate://crates.io/thread_local/0.2.7 \
crate://crates.io/serde/0.9.9 \
crate://crates.io/serde_codegen_internals/0.14.0 \
crate://crates.io/serde_derive/0.9.9 \
crate://crates.io/serde_json/0.9.8 \
crate://crates.io/syn/0.11.7 \
crate://crates.io/synom/0.11.0 \
crate://crates.io/time/0.1.36 \
crate://crates.io/toml/0.2.1 \
crate://crates.io/traitobject/0.0.1 \
crate://crates.io/typeable/0.1.2 \
crate://crates.io/unicase/1.4.0 \
crate://crates.io/unicode-bidi/0.2.5 \
crate://crates.io/unicode-normalization/0.1.4 \
crate://crates.io/unicode-xid/0.0.4 \
crate://crates.io/unix_socket/0.5.0 \
crate://crates.io/untrusted/0.3.2 \
crate://crates.io/url/1.4.0 \
crate://crates.io/user32-sys/0.2.0 \
crate://crates.io/utf8-ranges/0.1.3 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi-build/0.1.1 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/ws/0.5.3 \
crate://crates.io/ws2_32-sys/0.2.1 \
git://github.com/advancedtelematic/rvi_sota_client \
"
SRC_URI[index.md5sum] = "79f10f436dbf26737cc80445746f16b4"
SRC_URI[index.sha256sum] = "86114b93f1f51aaf0aec3af0751d214b351f4ff9839ba031315c1b19dcbb1913"

SYSTEMD_SERVICE_${PN} = "sota_client.service sota_client_autoprovision.service"

DEPENDS += " openssl dbus "
RDEPENDS_${PN} = " libcrypto \
                   libssl \
                   dbus \
                   bash \
                   lshw \
                   jq \
                   curl \
                   python \
                   python-canonicaljson \
                   python-json \
                   "

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
  install -m 0755 ${S}/run/sota_ostree.sh ${D}${bindir}
  install -m 0755 ${S}/run/sota_prov.sh ${D}${bindir}
  install -m 0755 ${S}/run/canonical_json.py ${D}${bindir}

  if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
    install -d ${D}/${systemd_unitdir}/system
    if [ -n "$SOTA_AUTOPROVISION_CREDENTIALS" ]; then
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

  if [ -n "$SOTA_AUTOPROVISION_CREDENTIALS" ]; then
    install -d ${D}/var
    install -d ${D}/var/sota
    install -m 0655 $SOTA_AUTOPROVISION_CREDENTIALS ${D}/var/sota/sota_provisioning_credentials.p12
    echo "SOTA_GATEWAY_URI=$SOTA_AUTOPROVISION_URL" > ${D}/var/sota/sota_provisioning_url.env
  fi

}
