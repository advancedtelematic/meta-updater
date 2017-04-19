#!/bin/sh

SCRIPT="envsetup.sh"

MACHINE=$1

if [ "$#" -lt 1 ]; then
	echo "Usage: ${SCRIPT} <machine> [builddir]"
	return -1
elif [ "$#" -eq 2 ]; then
	BUILDDIR=$2
else
	BUILDDIR=build
fi
BULDDIR=$2

# detect if this script is sourced: see http://stackoverflow.com/a/38128348/6255594
SOURCED=0
if [ -n "$ZSH_EVAL_CONTEXT" ]; then
        [[ $ZSH_EVAL_CONTEXT =~ :file$ ]] && { SOURCED=1; SOURCEDIR=$(cd $(dirname -- $0) && pwd -P); }
elif [ -n "$KSH_VERSION" ]; then
        [[ "$(cd $(dirname -- $0) && pwd -P)/$(basename -- $0)" != "$(cd $(dirname -- ${.sh.file}) && pwd -P)/$(basename -- ${.sh.file})" ]] && { SOURCED=1; SOURCEDIR=$(cd $(dirname -- ${.sh.file}) && pwd -P); }
elif [ -n "$BASH_VERSION" ]; then
        [[ $0 != "$BASH_SOURCE" ]] && { SOURCED=1; SOURCEDIR=$(cd $(dirname -- $BASH_SOURCE) && pwd -P); }
fi

if [ $SOURCED -ne 1 ]; then
        unset SOURCED
        unset SOURCEDIR
    echo "Error: this script needs to be sourced in a supported shell" >&2
    echo "Please check that the current shell is bash, zsh or ksh and run this script as '. $0 <args>'" >&2
    exit -1
fi

SCRIPTDIR=$(cd $(dirname $BASH_SOURCE) && pwd -P)
METADIR=$(cd $(dirname $BASH_SOURCE)/../.. && pwd -P)

if ! [[ -e ${SCRIPTDIR}/../conf/include/local/sota_${MACHINE}.inc && -e ${SCRIPTDIR}/../conf/include/bblayers/sota_${MACHINE}.inc ]]; then
	echo "Error: invalid machine: ${MACHINE}" >&2
	return -1
fi

if [ -e ${BUILDDIR}/conf/local.conf ]; then
	source $METADIR/poky/oe-init-build-env ${BUILDDIR}
else
	source $METADIR/poky/oe-init-build-env ${BUILDDIR}
	echo "METADIR  := \"\${@os.path.abspath('${METADIR}')}\"" >> conf/bblayers.conf
	cat ${METADIR}/meta-updater/conf/include/bblayers/sota.inc >> conf/bblayers.conf
	cat ${METADIR}/meta-updater/conf/include/bblayers/sota_${MACHINE}.inc >> conf/bblayers.conf
	echo "include conf/include/local/sota_${MACHINE}.inc" >> conf/local.conf
	echo "include conf/distro/sota.conf.inc" >> conf/local.conf
	echo "DISTRO = \"poky-sota-systemd\"" >> conf/local.conf
fi

