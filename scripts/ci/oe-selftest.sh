#!/bin/bash

# run meta-updater's oe-selftests

set -euo pipefail
set -x

TEST_MACHINE=${TEST_MACHINE:-qemux86-64}
TEST_BUILD_DIR=${TEST_BUILD_DIR:-build}
TEST_REPO_DIR=${TEST_REPO_DIR:-updater-repo}

(
set +euo pipefail
set +x
. "${TEST_REPO_DIR}/meta-updater/scripts/envsetup.sh" "${TEST_MACHINE}" "${TEST_BUILD_DIR}"

oe-selftest -r updater
)
