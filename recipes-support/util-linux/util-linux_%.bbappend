PACKAGES_append_class-native = "util-linux-agetty-native util-linux-fdisk-native util-linux-cfdisk-native util-linux-sfdisk-native \
             util-linux-swaponoff-native util-linux-losetup-native util-linux-umount-native \
             util-linux-mount-native util-linux-readprofile-native util-linux-uuidd-native \
             util-linux-uuidgen-native util-linux-lscpu-native util-linux-fsck-native util-linux-blkid \
             util-linux-mkfs-native util-linux-mcookie-native util-linux-reset-native \
             util-linux-mkfs.cramfs-native util-linux-fsck.cramfs-native util-linux-fstrim-native \
             util-linux-partx-native ${PN}-bash-completion-native util-linux-hwclock \
             util-linux-findfs-native util-linux-getopt-native util-linux-sulogin-native \
             ${@bb.utils.contains('PACKAGECONFIG', 'pylibmount', 'util-linux-pylibmount-native', '', d)}"

