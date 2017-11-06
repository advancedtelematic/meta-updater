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

HOSTTOOLS_NONFATAL += " repo "

# Write build information to target filesystem
buildinfo () {
  if [ $(which repo) ]; then
    repo manifest --revision-as-HEAD -o ${IMAGE_ROOTFS}${sysconfdir}/manifest.xml
  else
    echo "Android repo tool not food; manifest not copied."
  fi
}

IMAGE_PREPROCESS_COMMAND += "buildinfo;"
