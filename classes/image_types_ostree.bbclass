# OSTree deployment

inherit image_types

IMAGE_DEPENDS_ostree = "ostree-native:do_populate_sysroot \ 
			virtual/kernel:do_deploy \
			${INITRAMFS_IMAGE}:do_rootfs"

# Please redefine OSTREE_REPO in your local.conf in order to have a persistent
#   OSTree repo
OSTREE_REPO ?= "${DEPLOY_DIR_IMAGE}/ostree_repo"
export OSTREE_REPO

# OSTREE_BRANCHNAME can also be redefined
OSTREE_BRANCHNAME ?= "${IMAGE_BASENAME}"
export OSTREE_BRANCHNAME

IMAGE_CMD_ostree () {
	OSTREE_ROOTFS=`mktemp -d ${WORKDIR}/ostree-root-XXXXX`
	cp -rp ${IMAGE_ROOTFS}/* ${OSTREE_ROOTFS}
	cd ${OSTREE_ROOTFS}

	# Create sysroot directory to which physical sysroot will be mounted
	mkdir sysroot
	ln -sf /sysroot/ostree ostree
	ln -sf /sysroot/tmp tmp

	mkdir -p usr/rootdirs
	mkdir -p var/rootdirs

	# Implement UsrMove
	dirs="bin sbin lib"

	for dir in ${dirs} ; do
		if [ -d ${dir} ] && [ ! -L ${dir} ] ; then 
			mv ${dir} usr/rootdirs/
			rm -rf ${dir}
			ln -sf /usr/rootdirs/${dir} ${dir}
		fi
	done

	# Move persistent directories to /var
	dirs="home opt mnt media srv"

	for dir in ${dirs}; do
		if [ -d ${dir} ] && [ ! -L ${dir} ]; then
			mv ${dir} var/rootdirs/
			ln -sf /var/rootdirs/${dir} ${dir}
		fi
	done

	if [ -d root ] && [ ! -L root ]; then
		mv root var/roothome
		ln -sf /var/roothome root
	fi

	# Creating boot directories is required for "ostree admin deploy"

	mkdir -p boot/loader.0
	mkdir -p boot/loader.1
	ln -sf boot/loader.0 boot/loader

	checksum=`sha256sum ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE} | cut -f 1 -d " "`

	cp ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE} boot/vmlinuz-${checksum}
	cp ${DEPLOY_DIR_IMAGE}/${INITRD_IMAGE}-${MACHINE}.cpio.gz boot/initramfs-${checksum}

	cd ${WORKDIR}

	# Create a tarball that can be then commited to OSTree repo
	OSTREE_TAR=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.ostree.tar.bz2 
	tar -C ${OSTREE_ROOTFS} -cjf ${OSTREE_TAR} .
	rm -rf ${OSTREE_ROOTFS}
	
	if [ ! -d ${OSTREE_REPO} ]; then
		ostree --repo=${OSTREE_REPO} init --mode=archive-z2
	fi

	# Commit the result
	ostree --repo=${OSTREE_REPO} commit \
	       --tree=tar=${OSTREE_TAR} \
	       --skip-if-unchanged \
	       --branch=${OSTREE_BRANCHNAME} \
	       --subject="Commit-id: ${IMAGE_NAME}"

}

