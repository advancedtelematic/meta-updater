#!/bin/bash -x

echo "Device --> $DEVICE"
echo "Platform --> $PLATFORM"
echo "Test --> $TEST"
echo "REPO_BRANCH --> $REPO_BRANCH"
echo "BITBAKE_CACHE -- $BITBAKE_CACHE"

export DEVICE=${DEVICE:-'qemux86-64'}
export LANG=en_US.UTF-8
export REPO_BRANCH=${REPO_BRANCH:-'master'}

repo_sync()
{
if [ "${PLATFORM}" == "agl" ] || [ "${PLATFORM}" == "AGL" ]
then
	repo init -b eel -m default.xml -u https://gerrit.automotivelinux.org/gerrit/AGL/AGL-repo.git
else
	repo init -u https://github.com/advancedtelematic/updater-repo.git
fi

echo "Manifest before update"
REPO_MANIFEST=".repo/manifests/$REPO_BRANCH.xml"
cat $REPO_MANIFEST

METAUPDATER_REV=`git rev-parse HEAD`

sed -i 's/meta-updater" remote="github" revision="'$REPO_BRANCH'"/meta-updater" remote="github" revision="'$METAUPDATER_REV'"/' $REPO_MANIFEST

echo "Manifest after update"
cat $REPO_MANIFEST

repo init -m ${REPO_BRANCH}.xml
repo sync

if [ "${PLATFORM}" == "agl" ] || [ "${PLATFORM}" == "AGL" ]
then
	source meta-agl/scripts/aglsetup.sh -m $DEVICE agl-demo agl-appfw-smack agl-sota
else
	source meta-updater/scripts/envsetup.sh $DEVICE
fi
}

customize_build()
{
CONF_FILE_PATH="conf/local.conf"
if [ -f "$CONF_FILE_PATH" ]
then
	echo "SOTA_PACKED_CREDENTIALS = \"${CI_PROJECT_DIR}/data/credentials.zip\"" >> $CONF_FILE_PATH
	echo 'OSTREE_BRANCHNAME = "ostree_qemu_from_gitlab"' >> $CONF_FILE_PATH
	echo "DL_DIR = \"${BITBAKE_CACHE}\""  >> $CONF_FILE_PATH
	echo "SSTATE_DIR = \"${BITBAKE_CACHE}\""  >> $CONF_FILE_PATH
	echo 'IMAGE_INSTALL_append = " vim dropbear"' >> $CONF_FILE_PATH
	echo 'SANITY_TESTED_DISTROS = ""' >> conf/local.conf
	cat $CONF_FILE_PATH
else
	echo "$CONF_FILE_PATH not found."
	exit 1
fi
touch conf/sanity.conf
}

run_command()
{
if [ "${PLATFORM}" == "agl" ] || [ "${PLATFORM}" == "AGL" ]; then
   bitbake agl-demo-platform
elif [ "${TEST}" == "oe-selftest" ] || [ "${TEST}" == "OE-SELFTEST" ]; then
   oe-selftest -r updater
elif [ "${DEVICE}" == "raspberrypi3" ]; then
   bitbake rpi-basic-image
elif [ "${DEVICE}" == "qemux86-64" ]; then
   bitbake core-image-minimal
else
   echo "Unknown parameter provided"
   exit 1
fi
}

repo_sync
customize_build
run_command
df -h