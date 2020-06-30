DEPENDS += "u-boot-mkimage-native"

do_deploy_append_qoriq() {

    cat > ${DEPLOYDIR}/ls1043ardb_boot.txt << EOF
load mmc 0:2 \${load_addr} /boot/loader/uEnv.txt
env import -t \${fileaddr} \${filesize}
load mmc 0:2 \${load_addr} /boot\${kernel_image}
bootm \${load_addr}
EOF

    mkimage -A arm64 -O linux -T script -d ${DEPLOYDIR}/ls1043ardb_boot.txt ${DEPLOYDIR}/ls1043ardb_boot.scr
}
