do_install_append() {
  if [ -n "${SOTA_SECONDARY_CONFIG_DIR}" ]; then
    AKTUALIZR_CMDLINE_PARAMETERS="${AKTUALIZR_CMDLINE_PARAMETERS} --secondary-configs-dir /etc/sota/ecus"
  fi
  echo "AKTUALIZR_CMDLINE_PARAMETERS=${AKTUALIZR_CMDLINE_PARAMETERS}" > ${D}${libdir}/sota/sota.env
}

FILES_${PN}_append = " ${libdir}/sota/sota.env"
