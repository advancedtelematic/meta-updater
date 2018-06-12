#!/bin/bash

set -euo pipefail
set -x

TEST_MACHINE=${TEST_MACHINE:-qemux86-64}
TEST_BUILD_DIR=${TEST_BUILD_DIR:-build}
TEST_REPO_DIR=${TEST_REPO_DIR:-updater-repo}

TEST_AKTUALIZR_DIR=${TEST_AKTUALIZR_DIR:-.}
TEST_LOCAL_CONF_APPEND=${TEST_LOCAL_CONF_APPEND:-}
TEST_AKTUALIZR_BRANCH=${TEST_AKTUALIZR_BRANCH:-master}
TEST_AKTUALIZR_REV=${TEST_AKTUALIZR_REV:-$(GIT_DIR="${TEST_AKTUALIZR_DIR}/.git" git rev-parse "${TEST_AKTUALIZR_BRANCH}")}

# remove existing local.conf, keep
rm -rf "${TEST_BUILD_DIR}/conf.old" || true
mv "${TEST_BUILD_DIR}/conf" "${TEST_BUILD_DIR}/conf.old" || true

(
set +euo pipefail
set +x
echo ">> Running envsetup.sh"
. "${TEST_REPO_DIR}/meta-updater/scripts/envsetup.sh" "${TEST_MACHINE}" "${TEST_BUILD_DIR}"
)

if [[ -n $TEST_LOCAL_CONF_APPEND ]]; then
    echo ">> Appending to local.conf"
    REMOTE_AKTUALIZR_BRANCH=$(sed 's#^[^/]*/##g' <<< "$TEST_AKTUALIZR_BRANCH")
    cat "$TEST_LOCAL_CONF_APPEND" | \
        sed "s/\$<rev-sha1>/$TEST_AKTUALIZR_REV/g" | \
        sed "s/\$<rev-branch>/$REMOTE_AKTUALIZR_BRANCH/g" \
        >> "${TEST_BUILD_DIR}/conf/local.conf"
fi
