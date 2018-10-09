#!/bin/sh

if pkcs11-tool --module=/usr/lib/softhsm/libsofthsm2.so -O; then
	# The token has already been initialized, exit
	exit 0
fi

if ! ls /var/sota/import/pkey.pem /var/sota/import/client.pem; then
	# Key/certificate pair is not present, repeat
	exit 1
fi

mkdir -p /var/lib/softhsm/tokens
softhsm2-util --init-token --slot 0 --label "Virtual token" --pin 1234 --so-pin 1234

openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in /var/sota/import/pkey.pem -out /var/sota/import/pkey.p8
softhsm2-util --import /var/sota/import/pkey.p8 --label "pkey" --id 02 --token 'Virtual token' --pin 1234
openssl x509 -outform der -in /var/sota/import/client.pem -out /var/sota/import/client.der
pkcs11-tool --module=/usr/lib/softhsm/libsofthsm2.so --id 1 --write-object /var/sota/import/client.der --type cert --login --pin 1234

# Import UPTANE keypair if it exists
if [ -f /var/sota/import/ecukey.pem ]; then
	openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in /var/sota/import/ecukey.pem -out /var/sota/import/ecukey.p8
	softhsm2-util --import /var/sota/import/ecukey.p8 --label "uptanekey" --id 03 --token 'Virtual token' --pin 1234
fi

exit 0
