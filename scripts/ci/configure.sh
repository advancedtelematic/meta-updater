#!/bin/bash

set -euo pipefail
set -x

TEST_MACHINE=${TEST_MACHINE:-qemux86-64}
TEST_BUILD_DIR=${TEST_BUILD_DIR:-build}
TEST_REPO_DIR=${TEST_REPO_DIR:-updater-repo}

TEST_AKTUALIZR_DIR=${TEST_AKTUALIZR_DIR:-.}
TEST_AKTUALIZR_BRANCH=${TEST_AKTUALIZR_BRANCH:-master}
TEST_AKTUALIZR_REV=${TEST_AKTUALIZR_REV:-$(GIT_DIR="${TEST_AKTUALIZR_DIR}/.git" git rev-parse "${TEST_AKTUALIZR_BRANCH}")}

# move existing conf directory to backup, before generating a new one
rm -rf "${TEST_BUILD_DIR}/conf.old" || true
mv "${TEST_BUILD_DIR}/conf" "${TEST_BUILD_DIR}/conf.old" || true

(
set +euo pipefail
set +x
echo ">> Running envsetup.sh"
. "${TEST_REPO_DIR}/meta-updater/scripts/envsetup.sh" "${TEST_MACHINE}" "${TEST_BUILD_DIR}"
)

echo ">> Set aktualizr branch in bitbake's config"

cat << EOF > "${TEST_BUILD_DIR}/conf/site.conf"
SANITY_TESTED_DISTROS = ""
SRCREV_pn-aktualizr = "$TEST_AKTUALIZR_REV"
SRCREV_pn-aktualizr-native = "\${SRCREV_pn-aktualizr}"
BRANCH_pn-aktualizr = "$TEST_AKTUALIZR_BRANCH"
BRANCH_pn-aktualizr-native = "\${BRANCH_pn-aktualizr}"
EOF
