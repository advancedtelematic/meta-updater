SUMMARY = "Deploy SOTA credentials on the defice"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

FILES_${PN} += "${sysconfdir}/sota.toml"

ALLOW_EMPTY_${PN} = "1"

export SOTA_CREDENTIALS

do_install() {
	if [ -n "$SOTA_CREDENTIALS" ]; then
		if [ -f "$SOTA_CREDENTIALS" ]; then
			EXT=`basename $SOTA_CREDENTIALS | cut -d'.' -f2` 
			if [ "$EXT" != "toml" ]; then
				bbwarn "File\'s extension is not \'toml\', make sure you have the correct file"
			fi

			install -d ${D}${sysconfdir}
			cat $SOTA_CREDENTIALS | sed 's/^package_manager = .*$/package_manager = "ostree"/' > ${D}${sysconfdir}/sota.toml
			chmod 644 ${D}${sysconfdir}/sota.toml
		else
			bberror "File $SOTA_CREDENTIALS does not exist"
		fi
	fi
}

