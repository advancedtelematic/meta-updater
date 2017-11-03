inherit cargo systemd

DESCRIPTION = "rvi-sota-client recipe"
HOMEPAGE = "https://github.com/advancedtelematic/rvi_sota_client"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=65d26fcc2f35ea6a181ac777e42db1ea"

BBCLASSEXTEND = "native"

S = "${WORKDIR}/git"

SRC_URI[index.md5sum] = "6a635e8a081b4d4ba4cebffd721c2d7d"
SRC_URI[index.sha256sum] = "1913c41d4b8de89a931b6f9e418f83e70a083e12e6c247e8510ee932571ebae2"

# also update PV and SRC_URI crates when updating SRCREV
SRCREV = "be8ec83af2051a2b2499ce8878242771c65f0f1c"

PR = "1"

# generate with: `make package-version`
PV = "0.2.34-8-gbe8ec83"

# generate with: `make yocto-version`
SRC_URI = " \
git://github.com/advancedtelematic/rvi_sota_client \
file://sota-client-autoprovision.service \
file://sota-client-ostree.service \
file://sota-client-uptane.service \
file://sota-installer.service \
crate://crates.io/adler32/1.0.2 \
crate://crates.io/advapi32-sys/0.2.0 \
crate://crates.io/aho-corasick/0.6.3 \
crate://crates.io/ansi_term/0.9.0 \
crate://crates.io/antidote/1.0.0 \
crate://crates.io/atty/0.2.3 \
crate://crates.io/backtrace/0.3.3 \
crate://crates.io/backtrace-sys/0.1.15 \
crate://crates.io/base64/0.6.0 \
crate://crates.io/bincode/0.9.0 \
crate://crates.io/bit-set/0.4.0 \
crate://crates.io/bit-vec/0.4.4 \
crate://crates.io/bitflags/0.7.0 \
crate://crates.io/bitflags/0.9.1 \
crate://crates.io/block-buffer/0.2.0 \
crate://crates.io/byte-tools/0.2.0 \
crate://crates.io/byteorder/1.1.0 \
crate://crates.io/bytes/0.4.5 \
crate://crates.io/cc/1.0.1 \
crate://crates.io/cfg-if/0.1.2 \
crate://crates.io/chan/0.1.19 \
crate://crates.io/chan-signal/0.3.1 \
crate://crates.io/chrono/0.4.0 \
crate://crates.io/clap/2.26.2 \
crate://crates.io/coco/0.1.1 \
crate://crates.io/constant_time_eq/0.1.3 \
crate://crates.io/core-foundation/0.2.3 \
crate://crates.io/core-foundation-sys/0.2.3 \
crate://crates.io/crossbeam/0.3.0 \
crate://crates.io/crypt32-sys/0.2.0 \
crate://crates.io/crypto-mac/0.4.0 \
crate://crates.io/dbghelp-sys/0.2.0 \
crate://crates.io/dbus/0.5.4 \
crate://crates.io/digest/0.6.2 \
crate://crates.io/dtoa/0.4.2 \
crate://crates.io/either/1.2.0 \
crate://crates.io/env_logger/0.4.3 \
crate://crates.io/error-chain/0.10.0 \
crate://crates.io/fake-simd/0.1.2 \
crate://crates.io/filetime/0.1.14 \
crate://crates.io/foreign-types/0.2.0 \
crate://crates.io/fuchsia-zircon/0.2.1 \
crate://crates.io/fuchsia-zircon-sys/0.2.0 \
crate://crates.io/futures/0.1.16 \
crate://crates.io/gcc/0.3.54 \
crate://crates.io/generic-array/0.8.3 \
crate://crates.io/getopts/0.2.15 \
crate://crates.io/hex/0.2.0 \
crate://crates.io/hmac/0.4.2 \
crate://crates.io/httparse/1.2.3 \
crate://crates.io/hyper/0.10.13 \
crate://crates.io/hyper-native-tls/0.2.4 \
crate://crates.io/idna/0.1.4 \
crate://crates.io/iovec/0.1.1 \
crate://crates.io/itoa/0.3.4 \
crate://crates.io/kernel32-sys/0.2.2 \
crate://crates.io/language-tags/0.2.2 \
crate://crates.io/lazy_static/0.2.9 \
crate://crates.io/libc/0.2.32 \
crate://crates.io/libdbus-sys/0.1.1 \
crate://crates.io/libflate/0.1.11 \
crate://crates.io/log/0.3.8 \
crate://crates.io/maplit/0.1.5 \
crate://crates.io/matches/0.1.6 \
crate://crates.io/memchr/1.0.1 \
crate://crates.io/metadeps/1.1.2 \
crate://crates.io/mime/0.2.6 \
crate://crates.io/native-tls/0.1.4 \
crate://crates.io/net2/0.2.31 \
crate://crates.io/nodrop/0.1.9 \
crate://crates.io/num/0.1.40 \
crate://crates.io/num-integer/0.1.35 \
crate://crates.io/num-iter/0.1.34 \
crate://crates.io/num-traits/0.1.40 \
crate://crates.io/num_cpus/1.7.0 \
crate://crates.io/odds/0.2.25 \
crate://crates.io/openssl/0.9.19 \
crate://crates.io/openssl-sys/0.9.19 \
crate://crates.io/pem/0.4.1 \
crate://crates.io/percent-encoding/1.0.0 \
crate://crates.io/pkg-config/0.3.9 \
crate://crates.io/quote/0.3.15 \
crate://crates.io/rand/0.3.17 \
crate://crates.io/rayon/0.8.2 \
crate://crates.io/rayon-core/1.2.1 \
crate://crates.io/redox_syscall/0.1.31 \
crate://crates.io/redox_termios/0.1.1 \
crate://crates.io/regex/0.2.2 \
crate://crates.io/regex-syntax/0.4.1 \
crate://crates.io/reqwest/0.6.2 \
crate://crates.io/ring/0.12.1 \
crate://crates.io/rust-crypto/0.2.36 \
crate://crates.io/rustc-demangle/0.1.5 \
crate://crates.io/rustc-serialize/0.3.24 \
crate://crates.io/safemem/0.2.0 \
crate://crates.io/schannel/0.1.8 \
crate://crates.io/scopeguard/0.3.2 \
crate://crates.io/secur32-sys/0.2.0 \
crate://crates.io/security-framework/0.1.16 \
crate://crates.io/security-framework-sys/0.1.16 \
crate://crates.io/serde/1.0.15 \
crate://crates.io/serde_derive/1.0.15 \
crate://crates.io/serde_derive_internals/0.16.0 \
crate://crates.io/serde_json/1.0.3 \
crate://crates.io/serde_urlencoded/0.5.1 \
crate://crates.io/sha1/0.2.0 \
crate://crates.io/sha2/0.6.0 \
crate://crates.io/strsim/0.6.0 \
crate://crates.io/syn/0.11.11 \
crate://crates.io/synom/0.11.3 \
crate://crates.io/tar/0.4.13 \
crate://crates.io/tempdir/0.3.5 \
crate://crates.io/term_size/0.3.0 \
crate://crates.io/termion/1.5.1 \
crate://crates.io/textwrap/0.8.0 \
crate://crates.io/thread_local/0.3.4 \
crate://crates.io/time/0.1.38 \
crate://crates.io/toml/0.2.1 \
crate://crates.io/toml/0.4.5 \
crate://crates.io/traitobject/0.1.0 \
crate://crates.io/tungstenite/0.5.0 \
crate://crates.io/typeable/0.1.2 \
crate://crates.io/typenum/1.9.0 \
crate://crates.io/unicase/1.4.2 \
crate://crates.io/unicode-bidi/0.3.4 \
crate://crates.io/unicode-normalization/0.1.5 \
crate://crates.io/unicode-width/0.1.4 \
crate://crates.io/unicode-xid/0.0.4 \
crate://crates.io/unix_socket/0.5.0 \
crate://crates.io/unreachable/1.0.0 \
crate://crates.io/untrusted/0.5.1 \
crate://crates.io/url/1.5.1 \
crate://crates.io/utf-8/0.7.1 \
crate://crates.io/utf8-ranges/1.0.0 \
crate://crates.io/uuid/0.5.1 \
crate://crates.io/vcpkg/0.2.2 \
crate://crates.io/vec_map/0.8.0 \
crate://crates.io/version_check/0.1.3 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/winapi-build/0.1.1 \
crate://crates.io/ws2_32-sys/0.2.1 \
crate://crates.io/xattr/0.1.11 \
"
