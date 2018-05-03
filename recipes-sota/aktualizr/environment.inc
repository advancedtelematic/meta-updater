export SOTA_VIRTUAL_SECONDARIES

do_install_append() {
  for sec in ${SOTA_VIRTUAL_SECONDARIES}; do
    AKTUALIZR_PARAMETERS_VIRTUALSECS="${AKTUALIZR_PARAMETERS_VIRTUALSECS} --secondary-config $sec"
  done

  echo "AKTUALIZR_CMDLINE_PARAMETERS=${AKTUALIZR_PARAMETERS_VIRTUALSECS}" > ${D}${libdir}/sota/sota.env
}

FILES_${PN}_append = " ${libdir}/sota/sota.env"
