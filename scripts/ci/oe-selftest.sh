#!/bin/bash

# run meta-updater's oe-selftests

set -euo pipefail
set -x

TEST_MACHINE=${TEST_MACHINE:-qemux86-64}
TEST_BUILD_DIR=${TEST_BUILD_DIR:-build}
TEST_REPO_DIR=${TEST_REPO_DIR:-updater-repo}

OEST_ARGS=()
for v in "$@"; do
    OEST_ARGS+=("-r" "$v")
done

(
set +euo pipefail
set +x
METADIR=$(realpath "$TEST_REPO_DIR")
export METADIR
. "${TEST_REPO_DIR}/meta-updater/scripts/envsetup.sh" "${TEST_MACHINE}" "${TEST_BUILD_DIR}"

set -x
oe-selftest "${OEST_ARGS[@]}"
)
