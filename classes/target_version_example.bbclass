# Writes uses repo manifest version as a target version
#

HOSTTOOLS += " git "

deploy_target_version () {
  version=$(git --git-dir=${METADIR}/.repo/manifests/.git/ rev-parse HEAD)
  echo -n ${version} > ${STAGING_DATADIR_NATIVE}/target_version
}

IMAGE_PREPROCESS_COMMAND += "deploy_target_version;"
