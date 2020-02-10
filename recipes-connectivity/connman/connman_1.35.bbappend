RPROVIDES_${PN} += "virtual/network-configuration"

# patch to not create the resolv.conf symlink at run-time, as it's already
# handled in the recipe and messes up with ostree
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "file://0001-tmpfiles-script-do-not-create-the-resolv.conf-symlin.patch"
