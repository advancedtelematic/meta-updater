#!/bin/bash

SCRIPT="envsetup.sh"
MACHINE="$1"
BUILDDIR="build"

[[ "$#" -lt 1 ]] && { echo "Usage: ${SCRIPT} <machine> [builddir]"; return 1; }
[[ "$#" -eq 2 ]] && { BUILDDIR="$2"; }

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

if [[ ! -f "${BUILDDIR}/conf/local.conf" ]]; then
  source "$METADIR/poky/oe-init-build-env" "$BUILDDIR"

  echo "METADIR  := \"\${@os.path.abspath('${METADIR}')}\"" >> conf/bblayers.conf
  cat "${METADIR}/meta-updater/conf/include/bblayers/sota.inc" >> conf/bblayers.conf
  cat "${METADIR}/meta-updater/conf/include/bblayers/sota_${MACHINE}.inc" >> conf/bblayers.conf

  sed "s/##MACHINE##/$MACHINE/g" "${METADIR}/meta-updater/conf/local.conf.sample.append" >> conf/local.conf
else
  source "$METADIR/poky/oe-init-build-env" "$BUILDDIR"
fi
