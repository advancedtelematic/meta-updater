#!/bin/bash
set -euo pipefail

parentdir="$(dirname "$0")"

# Does NOT include garage-sign, anything used only for testing (i.e. strace and
# gtest), any of the git submodules, all of which are also only used for
# testing (tuf-test-vectors, isotp-c, ostreesysroot, and HdrHistogram_c), or
# any other third party modules included directly into the source tree
# (jsoncpp, open62541, picojson). Also check libp11, dpkg, and systemd since
# those are common dependencies not enabled by default.
${parentdir}/find_packages.py aktualizr \
                              aktualizr-native \
                              aktualizr-auto-prov \
                              aktualizr-implicit-prov \
                              aktualizr-ca-implicit-prov \
                              aktualizr-hsm-prov \
                              aktualizr-disable-send-ip \
                              aktualizr-example-interface \
                              aktualizr-log-debug \
                              libp11 \
                              dpkg \
                              systemd

