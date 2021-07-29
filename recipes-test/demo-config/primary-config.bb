DESCRIPTION = "Sample configuration for an Uptane Primary to support IP/Posix Secondary"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

require shared-conf.inc

inherit allarch

PRIMARY_SECONDARIES ?= "${SECONDARY_IP}:${SECONDARY_PORT}"

SRC_URI = "\
    file://30-secondary-config.toml \
    file://ip_secondary_config.json \
    ${@('file://' + d.getVar('SOTA_SECONDARY_CONFIG')) if d.getVar('SOTA_SECONDARY_CONFIG') else ''} \
    "

def get_secondary_addrs(d):
    import json

    secondaries = d.getVar('PRIMARY_SECONDARIES')
    sec_array = []
    for secondary in secondaries.split():
        sec_array.append({"addr": secondary})

    return json.dumps(sec_array) 

do_install () {

    if [ ! -n "${SOTA_SECONDARY_CONFIG}" ]; then
        bbwarn "SOTA_SECONDARY_CONFIG hasn't been specified in the local config, generate a default one"

        IP_SECONDARY_CONFIG_FILE=${WORKDIR}/ip_secondary_config.json
        IP_SECONDARY_ADDRS='${@get_secondary_addrs(d)}'
    else
        bbwarn "SOTA_SECONDARY_CONFIG has been specified in the local config: ${SOTA_SECONDARY_CONFIG}"

        IP_SECONDARY_CONFIG_FILE=${SOTA_SECONDARY_CONFIG}
    fi

    if [ ! -f $IP_SECONDARY_CONFIG_FILE ]; then
        bbfatal "Secondary config file does not exist: $IP_SECONDARY_CONFIG_FILE"
    fi

    SECONDARY_CONFIG_DEST_DIR="${D}${sysconfdir}/sota/ecus"
    SECONDARY_CONFIG_DEST_FILEPATH=$SECONDARY_CONFIG_DEST_DIR/$(basename -- $IP_SECONDARY_CONFIG_FILE)
    SECONDARY_CONFIG_FILEPATH_ON_IMAGE="${sysconfdir}/sota/ecus/$(basename -- $IP_SECONDARY_CONFIG_FILE)"

    # install the secondary configuration file (json)
    install -m 0700 -d $SECONDARY_CONFIG_DEST_DIR
    install -m 0644 $IP_SECONDARY_CONFIG_FILE $SECONDARY_CONFIG_DEST_DIR

    # if SOTA_SECONDARY_CONFIG/secondary config file is not defined in the local conf 
    # then a default template is used and filled with corresponding configuration variable values 
    if [ ! -n "${SOTA_SECONDARY_CONFIG}" ]; then
        sed -i -e "s|@PORT@|${PRIMARY_PORT}|g" \
               -e "s|@TIMEOUT@|${PRIMARY_WAIT_TIMEOUT}|g" \
               -e "s|@ADDR_ARRAY@|$IP_SECONDARY_ADDRS|g" $SECONDARY_CONFIG_DEST_FILEPATH
    fi

    # install aktualizr config file (toml) that points to the secondary config file, so aktualizr is aware about it
    install -m 0700 -d ${D}${libdir}/sota/conf.d
    install -m 0644 ${WORKDIR}/30-secondary-config.toml ${D}${libdir}/sota/conf.d
    sed -i "s|@CFG_FILEPATH@|$SECONDARY_CONFIG_FILEPATH_ON_IMAGE|g" ${D}${libdir}/sota/conf.d/30-secondary-config.toml
}

FILES:${PN} = " \
                ${libdir}/sota/conf.d/* \
                ${sysconfdir}/sota/ecus/* \
                "

# vim:set ts=4 sw=4 sts=4 expandtab:
