#!/bin/bash
set -euo pipefail

parentdir="$(dirname "$0")"

# Does NOT include garage-sign, anything used only for testing (i.e. strace and
# gtest), any of the git submodules, all of which are also only used for
# testing (tuf-test-vectors, isotp-c, ostreesysroot, and HdrHistogram_c), or
# any other third party modules included directly into the source tree
# (jsoncpp, open62541, picojson). Also check libp11, dpkg, and systemd since
# those are common dependencies not enabled by default.
${parentdir}/find_dependencies.py aktualizr
${parentdir}/find_dependencies.py aktualizr-shared-prov
${parentdir}/find_dependencies.py aktualizr-shared-prov-creds
${parentdir}/find_dependencies.py aktualizr-device-prov
${parentdir}/find_dependencies.py aktualizr-device-prov-creds
${parentdir}/find_dependencies.py aktualizr-device-prov-hsm
${parentdir}/find_dependencies.py aktualizr-auto-reboot
${parentdir}/find_dependencies.py aktualizr-disable-send-ip
${parentdir}/find_dependencies.py aktualizr-log-debug
${parentdir}/find_dependencies.py aktualizr-polling-interval
${parentdir}/find_dependencies.py libp11
${parentdir}/find_dependencies.py dpkg
${parentdir}/find_dependencies.py systemd

