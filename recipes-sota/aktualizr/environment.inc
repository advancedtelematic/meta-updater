do_install_append() {
  if [ -n "${SOTA_LEGACY_SECONDARY_INTERFACE}" ]; then
    AKTUALIZR_PARAMETERS_LEGACYSEC="--legacy-interface ${SOTA_LEGACY_SECONDARY_INTERFACE}";
  fi

  AKTUALIZR_PARAMETERS_CONFIGFILE="--config /usr/lib/sota/sota.toml"
  echo "AKTUALIZR_CMDLINE_PARAMETERS=${AKTUALIZR_PARAMETERS_CONFIGFILE} ${AKTUALIZR_PARAMETERS_LEGACYSEC}" > ${D}${libdir}/sota/sota.env
}

FILES_${PN}_append = " ${libdir}/sota/sota.env"
