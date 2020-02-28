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
METADIR=$(realpath "$TEST_REPO_DIR")
export METADIR
. "${TEST_REPO_DIR}/meta-updater/scripts/envsetup.sh" "${TEST_MACHINE}" "${TEST_BUILD_DIR}"

set -x

# work poky around bug on sumo and thud
# see https://git.yoctoproject.org/cgit/cgit.cgi/poky/commit/?id=d3a94e5b9b3c107cf54d5639071cc6609c002f67
mkdir -p "tmp/log"

# This is apparently required here now as well.
git config --global user.email "meta-updater-ci@example.org"
git config --global user.name "meta-updater-ci"

oe-selftest -r "$@"
)
