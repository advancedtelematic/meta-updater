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

METADIR="${SOURCEDIR}/../.."

if [[ ! -f "${BUILDDIR}/conf/local.conf" ]]; then
  if [ -z "$TEMPLATECONF" ] && [ -d ${METADIR}/meta-updater-${MACHINE}/conf ]; then
    # Use the template configurations for the specified machine
    TEMPLATECONF=${METADIR}/meta-updater-${MACHINE}/conf
    source "$METADIR/poky/oe-init-build-env" "$BUILDDIR"
    unset TEMPLATECONF
  else
    # Use the default configurations or TEMPLATECONF set by the user
    source "$METADIR/poky/oe-init-build-env" "$BUILDDIR"
  fi
  echo "METADIR  := \"\${@os.path.abspath('${METADIR}')}\"" >> conf/bblayers.conf
  cat "${METADIR}/meta-updater/conf/include/bblayers/sota.inc" >> conf/bblayers.conf
  cat "${METADIR}/meta-updater/conf/include/bblayers/sota_${MACHINE}.inc" >> conf/bblayers.conf
  echo "MACHINE = \"${MACHINE}\"" >> conf/local.conf
  echo "DISTRO = \"poky-sota-systemd\"" >> conf/local.conf
else
  source "$METADIR/poky/oe-init-build-env" "$BUILDDIR"
fi
