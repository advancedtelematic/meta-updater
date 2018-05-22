#!/bin/sh
set -eu

# -------------------------------------------

log_info() { echo "$0[$$]: $*" >&2; }
log_error() { echo "$0[$$]: ERROR $*" >&2; }

do_mount_fs() {
	log_info "mounting FS: $*"
	[[ -e /proc/filesystems ]] && { grep -q "$1" /proc/filesystems || { log_error "Unknown filesystem"; return 1; } }
	[[ -d "$2" ]] || mkdir -p "$2"
	[[ -e /proc/mounts ]] && { grep -q -e "^$1 $2 $1" /proc/mounts && { log_info "$2 ($1) already mounted"; return 0; } }
	mount -t "$1" "$1" "$2"
}

bail_out() {
	log_error "$@"
	log_info "Rebooting..."
	#exec reboot -f
	exec sh
}

get_ostree_sysroot() {
	for opt in $(cat /proc/cmdline); do
		arg=$(echo "$opt" | cut -d'=' -f1)
		if [ "$arg" == "ostree_root" ]; then
			echo "$opt" | cut -d'=' -f2-
			return
		fi
	done
	echo "LABEL=otaroot"
}

export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/usr/lib/ostree

log_info "Starting OSTree initrd script"

do_mount_fs proc /proc
do_mount_fs sysfs /sys
do_mount_fs devtmpfs /dev
do_mount_fs devpts /dev/pts
do_mount_fs tmpfs /dev/shm
do_mount_fs tmpfs /tmp
do_mount_fs tmpfs /run

# check if smack is active (and if so, mount smackfs)
grep -q smackfs /proc/filesystems && {
	do_mount_fs smackfs /sys/fs/smackfs

	# adjust current label and network label
	echo System >/proc/self/attr/current
	echo System >/sys/fs/smackfs/ambient
}

mkdir -p /sysroot
ostree_sysroot=$(get_ostree_sysroot)

mount "$ostree_sysroot" /sysroot || {
	# The SD card in the R-Car M3 takes a bit of time to come up
	# Retry the mount if it fails the first time
	log_info "Mounting $ostree_sysroot failed, waiting 5s for the device to be available..."
	sleep 5
	mount "$ostree_sysroot" /sysroot || bail_out "Unable to mount $ostree_sysroot as physical sysroot"
}
ostree-prepare-root /sysroot

# move mounted devices to new root
cd /sysroot
for x in dev proc run; do
	log_info "Moving /$x to new rootfs"
	mount -o move "/$x" "$x"
done

# switch to new rootfs
log_info "Switching to new rootfs"
mkdir -p run/initramfs

pivot_root . run/initramfs || bail_out "pivot_root failed."

log_info "Launching target init"

exec chroot . sh -c 'umount /run/initramfs; exec /sbin/init' \
	  <dev/console >dev/console 2>&1

