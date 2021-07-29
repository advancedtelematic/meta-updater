#!/bin/bash

set -euo pipefail
set -x

TEST_MACHINE=${TEST_MACHINE:-qemux86-64}
TEST_BUILD_DIR=${TEST_BUILD_DIR:-build}
TEST_REPO_DIR=${TEST_REPO_DIR:-updater-repo}
TEST_BITBAKE_COMMON_DIR=${TEST_BITBAKE_COMMON_DIR:-}

TEST_AKTUALIZR_REMOTE=${TEST_AKTUALIZR_REMOTE:-}
TEST_AKTUALIZR_TAG=${TEST_AKTUALIZR_TAG:-}
if [ -n "$TEST_AKTUALIZR_REMOTE" ]; then
    if [ -n "$TEST_AKTUALIZR_TAG" ]; then
        TEST_AKTUALIZR_BRANCH=""
        TEST_AKTUALIZR_REV=""
    else
        TEST_AKTUALIZR_DIR=${TEST_AKTUALIZR_DIR:-.}
        TEST_AKTUALIZR_BRANCH=${TEST_AKTUALIZR_BRANCH:-master}
        TEST_AKTUALIZR_REV=${TEST_AKTUALIZR_REV:-$(GIT_DIR="$TEST_AKTUALIZR_DIR/.git" git rev-parse "$TEST_AKTUALIZR_REMOTE/$TEST_AKTUALIZR_BRANCH")}
    fi
fi

TEST_AKTUALIZR_CREDENTIALS=${TEST_AKTUALIZR_CREDENTIALS:-}

# move existing conf directory to backup, before generating a new one
rm -rf "$TEST_BUILD_DIR/conf.old" || true
mv "$TEST_BUILD_DIR/conf" "$TEST_BUILD_DIR/conf.old" || true

(
set +euo pipefail
set +x
echo ">> Running envsetup.sh"
METADIR=$(realpath "$TEST_REPO_DIR")
export METADIR
. "$TEST_REPO_DIR/meta-updater/scripts/envsetup.sh" "$TEST_MACHINE" "$TEST_BUILD_DIR"
)

set +x

SITE_CONF="$TEST_BUILD_DIR/conf/site.conf"

echo ">> Set common bitbake config options"
cat << EOF > "$SITE_CONF"
SANITY_TESTED_DISTROS = ""
IMAGE_FEATURES += "ssh-server-openssh"

EOF

if [ -n "$TEST_AKTUALIZR_REMOTE" ]; then
    echo ">> Set aktualizr branch in bitbake's config"
    if [ -n "$TEST_AKTUALIZR_TAG" ]; then
        # tag case
        cat << EOF >> "$SITE_CONF"
SRCREV:pn-aktualizr = ""
SRCREV:pn-aktualizr-native = ""
BRANCH:pn-aktualizr = ";nobranch=1;tag=$TEST_AKTUALIZR_TAG"
BRANCH:pn-aktualizr-native = "\${BRANCH_pn-aktualizr}"
EOF
    else
        # branch case
        cat << EOF >> "$SITE_CONF"
SRCREV:pn-aktualizr = "$TEST_AKTUALIZR_REV"
SRCREV:pn-aktualizr-native = "\${SRCREV_pn-aktualizr}"
BRANCH:pn-aktualizr = "$TEST_AKTUALIZR_BRANCH"
BRANCH:pn-aktualizr-native = "\${BRANCH_pn-aktualizr}"
EOF
    fi
fi

if [[ -n $TEST_AKTUALIZR_CREDENTIALS ]]; then
    echo ">> Set aktualizr credentials"
    cat << EOF >> "$SITE_CONF"
SOTA_PACKED_CREDENTIALS = "$TEST_AKTUALIZR_CREDENTIALS"
EOF
fi

if [[ -n $TEST_BITBAKE_COMMON_DIR ]]; then
    echo ">> Set caching"
    SSTATE_DIR="$TEST_BITBAKE_COMMON_DIR/sstate-cache"
    DL_DIR="$TEST_BITBAKE_COMMON_DIR/downloads"
    mkdir -p "$SSTATE_DIR" "$DL_DIR"

    cat << EOF >> "$SITE_CONF"
SSTATE_DIR = "$SSTATE_DIR"
DL_DIR = "$DL_DIR"
EOF
fi

echo -e ">> Final configuration (site.conf):\\n"
cat "$SITE_CONF"
