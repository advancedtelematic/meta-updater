
do_install:append() {

    # Set the zabbix Server 
    if [ ! -z ${SOTA_COMM_CONF_ZABBIX_SERVER} ]; then
        sed -i "s/Server=\([0-9]\{1,3\}\.\)\{3\}[0-9]\{1,3\}/Server=${SOTA_COMM_CONF_ZABBIX_SERVER}/g" ${D}${sysconfdir}/zabbix_agentd.conf
        if ! grep -Fxq "Server=${SOTA_COMM_CONF_ZABBIX_SERVER}" ${D}${sysconfdir}/zabbix_agentd.conf; then
            echo -e '\nServer='${SOTA_COMM_CONF_ZABBIX_SERVER} >> ${D}${sysconfdir}/zabbix_agentd.conf
        fi
    fi

    # Set ServerActive
    if [ ! -z ${SOTA_COMM_CONF_ZABBIX_SERVERACTIVE} ]; then
        sed -i "s/ServerActive=\([0-9]\{1,3\}\.\)\{3\}[0-9]\{1,3\}/ServerActive=${SOTA_COMM_CONF_ZABBIX_SERVERACTIVE}/g" ${D}${sysconfdir}/zabbix_agentd.conf
        if ! grep -Fxq "ServerActive=${SOTA_COMM_CONF_ZABBIX_SERVERACTIVE}" ${D}${sysconfdir}/zabbix_agentd.conf; then
            echo -e '\nServerActive='${SOTA_COMM_CONF_ZABBIX_SERVERACTIVE} >> ${D}${sysconfdir}/zabbix_agentd.conf
        fi

    fi
}
