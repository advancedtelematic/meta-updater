#!/usr/bin/env bash

set -euo pipefail

set -x

REMOTE_SOURCE=${REMOTE_SOURCE:-https://github.com/advancedtelematic}
MANIFEST=${MANIFEST:-master}
CURRENT_PROJECT=${CURRENT_PROJECT:-meta-updater}

#CURRENT_REV=$(git rev-parse HEAD)
LOCAL_REPO=$PWD

mkdir -p updater-repo

cd updater-repo

repo init -m "${MANIFEST}.xml" -u "$REMOTE_SOURCE/updater-repo"

git -C .repo/manifests reset --hard

# patch manifest:
# - add a new "ats" remote that points to "$REMOTE_SOURCE"
# - change projects that contain "advancedtelematic" to use the ats remote
# - remove the current project from the manifest
MANIFEST_FILE=".repo/manifests/${MANIFEST}.xml"
xmlstarlet ed --omit-decl -L \
    -s "/manifest" -t elem -n "remote" -v "" \
    -i "/manifest/remote[last()]" -t attr -n "name" -v "ats" \
    -i "/manifest/remote[last()]" -t attr -n "fetch" -v "$REMOTE_SOURCE" \
    -i "/manifest/project[contains(@name, 'advancedtelematic')]" -t attr -n "remote" -v "ats" \
    -d "/manifest/project[@path=\"$CURRENT_PROJECT\"]" \
    "$MANIFEST_FILE"

# hack: sed on `advancedtelematic/` names, to remove this unwanted prefix
sed -i 's#name="advancedtelematic/#name="#g' "$MANIFEST_FILE"

repo manifest

repo forall -c 'git reset --hard ; git clean -fdx'

repo sync -d --force-sync

rm -f "$CURRENT_PROJECT"
ln -s "$LOCAL_REPO" "$CURRENT_PROJECT"

repo manifest -r
