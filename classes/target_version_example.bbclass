# Writes target version to be used by garage-sign

deploy_target_version () {
  version=$(git --git-dir=${METADIR}/.repo/manifests/.git/ rev-parse HEAD)
  echo -n ${version} > ${STAGING_DATADIR_NATIVE}/target_version
}

IMAGE_PREPROCESS_COMMAND += "deploy_target_version;"
