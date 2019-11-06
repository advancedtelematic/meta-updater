#!/bin/bash

SCRIPT="envsetup.sh"
MACHINE="$1"
BUILDDIR="build"
DISTRO="poky-sota-systemd"
BASE_CONF="local.conf.base.append"
declare -A supported_distros=( ["poky-sota-systemd"]="local.conf.systemd.append" ["poky-sota"]="local.conf.base.append" )

[[ "$#" -lt 1 ]] && { echo "Usage: ${SCRIPT} <machine> [builddir] [distro=< poky-sota-systemd | poky-sota >]"; return 1; }
[[ "$#" -ge 2 ]] && { BUILDDIR="$2"; }
[[ "$#" -eq 3 ]] && { DISTRO="$3"; }

# detect if this script is sourced: see http://stackoverflow.com/a/38128348/6255594
SOURCED=0
if [ -n "$ZSH_EVAL_CONTEXT" ]; then
  [[ "$ZSH_EVAL_CONTEXT" =~ :file$ ]] && { SOURCED=1; SOURCEDIR=$(cd "$(dirname -- "$0")" && pwd -P); }
elif [ -n "$BASH_VERSION" ]; then
  [[ "$0" != "${BASH_SOURCE[0]}" ]] && { SOURCED=1; SOURCEDIR=$(cd "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P); }
fi

if [[ $SOURCED -ne 1 ]]; then
  echo "Error: this script needs to be sourced in a supported shell" >&2
  echo "Please check that the current shell is bash or zsh and run this script as '. $0 <args>'" >&2
  exit 1
fi

METADIR=${METADIR:-${SOURCEDIR}/../..}
DISTRO_CONF=${supported_distros[$DISTRO]}
[[ -n $DISTRO_CONF ]] && { echo "Using $DISTRO_CONF for the specified distro $DISTRO"; } || { echo "The specified distro $DISTRO is not supported"; return 1; }

if [[ ! -f "${BUILDDIR}/conf/local.conf" ]]; then
  source "$METADIR/poky/oe-init-build-env" "$BUILDDIR"

  echo "METADIR  := \"\${@os.path.abspath('${METADIR}')}\"" >> conf/bblayers.conf
  cat "${METADIR}/meta-updater/conf/include/bblayers/sota.inc" >> conf/bblayers.conf
  cat "${METADIR}/meta-updater/conf/include/bblayers/sota_${MACHINE}.inc" >> conf/bblayers.conf

  sed -e "s/##MACHINE##/$MACHINE/g" \
      -e "s/##DISTRO##/$DISTRO/g" \
	 "${METADIR}/meta-updater/conf/$BASE_CONF" >> conf/local.conf

  if [ "$BASE_CONF" != "$DISTRO_CONF" ]; then
    cat "${METADIR}/meta-updater/conf/$DISTRO_CONF" >> conf/local.conf
  fi
else
  source "$METADIR/poky/oe-init-build-env" "$BUILDDIR"
fi
