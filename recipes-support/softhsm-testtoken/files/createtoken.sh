#!/bin/sh

if pkcs11-tool --module=/usr/lib/softhsm/libsofthsm2.so -O; then
	# The token has already been initialized, exit
	exit 0
fi

if ! ls /var/sota/token/pkey.pem /var/sota/token/client.pem; then
	# Key/certificate pair is not present, repeat
	mkdir -p /var/sota/token
	exit 1
fi

mkdir -p /var/lib/softhsm/tokens
softhsm2-util --init-token --slot 0 --label "Virtual token" --pin 1234 --so-pin 1234

pkcs11-tool --module=/usr/lib/softhsm/libsofthsm2.so --label 'Virtual token' --write-object /var/sota/token/pkey.pem --type privkey --login --pin 1234
openssl x509 -outform der -in /var/sota/token/client.pem -out /var/sota/token/client.der
pkcs11-tool --module=/usr/lib/softhsm/libsofthsm2.so --label 'Virtual token' --write-object /var/sota/token/client.der --type cert --login --pin 1234

exit 0
