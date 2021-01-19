# Writes the repo manifest to the target filesystem in /etc/manifest.xml
#
# Author: Phil Wise <phil@advancedtelematic.com>
# Usage: add "inherit image_repo_manifest" to your image file
# To reproduce a build, copy the /etc/manifest.xml to .repo/manifests/yourname.xml
# then run:
# repo init -m yourname.xml
# repo sync
# For more information, see:
# https://web.archive.org/web/20161224194009/https://wiki.cyanogenmod.org/w/Doc:_Using_manifests 

inherit python3native

# Write build information to target filesystem
buildinfo_manifest () {
  repotool=`which repo || true`
  if [ -n "$repotool" ]; then
    cd ${THISDIR} && python3 $repotool manifest --revision-as-HEAD -o ${IMAGE_ROOTFS}${sysconfdir}/manifest.xml || bbwarn "Android repo tool failed to run; manifest not copied"
  else
    bbwarn "Android repo tool not found; manifest not copied."
  fi
}

IMAGE_PREPROCESS_COMMAND += "buildinfo_manifest;"
