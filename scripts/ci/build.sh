#!/bin/bash

set -euo pipefail
set -x

TEST_MACHINE=${TEST_MACHINE:-qemux86-64}
TEST_BUILD_DIR=${TEST_BUILD_DIR:-build}
TEST_REPO_DIR=${TEST_REPO_DIR:-updater-repo}

IMAGE_NAME=${1:-core-image-minimal}

(
set +euo pipefail
set +x
METADIR=$(realpath "$TEST_REPO_DIR")
export METADIR
. "${TEST_REPO_DIR}/meta-updater/scripts/envsetup.sh" "${TEST_MACHINE}" "${TEST_BUILD_DIR}"

set -x
bitbake "${IMAGE_NAME}"
)
