# OSTree deployment

inherit image

IMAGE_DEPENDS_ostree = "ostree-native:do_populate_sysroot \ 
			virtual/kernel:do_deploy \
			${OSTREE_INITRAMFS_IMAGE}:do_image_ext4"

export OSTREE_REPO
export OSTREE_BRANCHNAME

RAMDISK_EXT ?= ".ext4.gz"
RAMDISK_EXT_arm ?= ".ext4.gz.u-boot"

OSTREE_KERNEL ??= "${KERNEL_IMAGETYPE}"

IMAGE_CMD_ostree () {
	if [ -z "$OSTREE_REPO" ]; then
		bbfatal "OSTREE_REPO should be set in your local.conf"
	fi

	if [ -z "$OSTREE_BRANCHNAME" ]; then
		bbfatal "OSTREE_BRANCHNAME should be set in your local.conf"
	fi

	OSTREE_ROOTFS=`mktemp -du ${WORKDIR}/ostree-root-XXXXX`
	cp -a ${IMAGE_ROOTFS} ${OSTREE_ROOTFS}
	chmod a+rx ${OSTREE_ROOTFS}
	sync

	cd ${OSTREE_ROOTFS}

	# Create sysroot directory to which physical sysroot will be mounted
	mkdir sysroot
	ln -sf sysroot/ostree ostree

	rm -rf tmp/*
	ln -sf sysroot/tmp tmp

	mkdir -p usr/rootdirs

	mv etc usr/
	# Implement UsrMove
	dirs="bin sbin lib"

	for dir in ${dirs} ; do
		if [ -d ${dir} ] && [ ! -L ${dir} ] ; then 
			mv ${dir} usr/rootdirs/
			rm -rf ${dir}
			ln -sf usr/rootdirs/${dir} ${dir}
		fi
	done
	
	if [ ! -d "usr/etc/tmpfiles.d" ]; then
		mkdir usr/etc/tmpfiles.d
	fi
	tmpfiles_conf=usr/etc/tmpfiles.d/00ostree-tmpfiles.conf

	echo "d /var/rootdirs 0755 root root -" >>${tmpfiles_conf}
	echo "L /var/rootdirs/home - - - - /sysroot/home" >>${tmpfiles_conf}
	# Preserve data in /home to be later copied to /sysroot/home by
	#   sysroot generating procedure
	mkdir -p usr/homedirs
	if [ -d "home" ] && [ ! -L "home" ]; then
		mv home usr/homedirs/home
		ln -sf var/rootdirs/home home
	fi

	# Move persistent directories to /var
	dirs="opt mnt media srv"

	for dir in ${dirs}; do
		if [ -d ${dir} ] && [ ! -L ${dir} ]; then
			if [ "$(ls -A $dir)" ]; then
				bbwarn "Data in /$dir directory is not preserved by OSTree. Consider moving it under /usr"
			fi
			echo "d /var/rootdirs/${dir} 0755 root root -" >>${tmpfiles_conf}
			rm -rf ${dir}
			ln -sf var/rootdirs/${dir} ${dir}
		fi
	done

	if [ -d root ] && [ ! -L root ]; then
		if [ "$(ls -A root)" ]; then
			bberror "Data in /root directory is not preserved by OSTree."
		fi
		echo "d /var/roothome 0755 root root -" >>${tmpfiles_conf}
		rm -rf root
		ln -sf var/roothome root
	fi

	# Creating boot directories is required for "ostree admin deploy"

	mkdir -p boot/loader.0
	mkdir -p boot/loader.1
	ln -sf boot/loader.0 boot/loader

	checksum=`sha256sum ${DEPLOY_DIR_IMAGE}/${OSTREE_KERNEL} | cut -f 1 -d " "`

	cp ${DEPLOY_DIR_IMAGE}/${OSTREE_KERNEL} boot/vmlinuz-${checksum}
	cp ${DEPLOY_DIR_IMAGE}/${OSTREE_INITRAMFS_IMAGE}-${MACHINE}${RAMDISK_EXT} boot/initramfs-${checksum}

	cd ${WORKDIR}

	# Create a tarball that can be then commited to OSTree repo
	OSTREE_TAR=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.ostree.tar.bz2 
	tar -C ${OSTREE_ROOTFS} --xattrs --xattrs-include='*' -cjf ${OSTREE_TAR} .
	sync

	rm -f ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.rootfs.ostree.tar.bz2
	ln -s ${IMAGE_NAME}.rootfs.ostree.tar.bz2 ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.rootfs.ostree.tar.bz2
	
	if [ ! -d ${OSTREE_REPO} ]; then
		ostree --repo=${OSTREE_REPO} init --mode=archive-z2
	fi

	# Commit the result
	ostree --repo=${OSTREE_REPO} commit \
	       --tree=dir=${OSTREE_ROOTFS} \
	       --skip-if-unchanged \
	       --branch=${OSTREE_BRANCHNAME} \
	       --subject="Commit-id: ${IMAGE_NAME}"

	rm -rf ${OSTREE_ROOTFS}
}

IMAGE_TYPEDEP_ostreepush = "ostree"
IMAGE_DEPENDS_ostreepush = "sota-tools-native:do_populate_sysroot"
IMAGE_CMD_ostreepush () {
	if [ ${OSTREE_PUSH_CREDENTIALS} ]; then
		garage-push --repo=${OSTREE_REPO} \
			    --ref=${OSTREE_BRANCHNAME} \
			    --credentials=${OSTREE_PUSH_CREDENTIALS}
	fi
}
