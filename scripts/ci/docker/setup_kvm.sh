#!/usr/bin/env bash

# This script makes the gid of the 'kvm' group to match the group
# owner of '/dev/kvm'
#
# These two are not guaranteed to match when a docker image starts
# with access to '/dev/kvm' that comes from the host

set -euo pipefail

kvm_gid=$(stat -c "%g" /dev/kvm)
groupmod -g "$kvm_gid" kvm
usermod -a -G kvm bitbake
ln -s /bin/true /usr/bin/kvm-ok
