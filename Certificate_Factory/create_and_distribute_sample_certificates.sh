#!/usr/bin/env bash

echo "This script will generate all required sample certificates and copies them to the backend and client modules, "
echo "which is required to make them work.\n\n"

echo "Old certificates will be deleted. Continue Y/N?"

read -r user_input

if [ "$user_input" != "Y" ] && [ "$user_input" != "y" ]; then
    echo "Script execution aborted."
    exit 1
fi

echo "Clean up old certificates files if there are any"
rm *.key
rm *.crt
rm *.csr
rm *.srl
rm *.p12
rm ../Backend/src/main/resources/*.p12
rm ../Client/src/main/resources/*.p12

echo "Creating the CA keyfiles"
openssl genrsa -out backendRootCA.key 2048
openssl genrsa -out clientRootCA.key 2048

echo "generate RootCA certificates"
openssl req -x509 -new -nodes -key backendRootCA.key -sha256 -days 1095 --config backendRootCA.conf -out backendRootCA.crt
openssl req -x509 -new -nodes -key clientRootCA.key -sha256 -days 1095 --config clientRootCA.conf -out clientRootCA.crt

echo "generate backend key file and server certificate signing request"
openssl genrsa -out backend.key 2048
openssl req -new -key backend.key -out backend.csr --config backendCertificateSigningReq.conf

echo "Next sign the backend server certificate with your backend RootCA"
openssl x509 -req -in backend.csr -CA backendRootCA.crt -CAkey backendRootCA.key -CAcreateserial -out backend.crt -days 365 -sha256

echo "convert X509 to PKCS12: backend.p12 and backendRootCA.p12 certificate (for the Server-Component)"
openssl pkcs12 -export -in backend.crt -inkey backend.key -out backend.p12 -name servercert -chain -CAfile backendRootCA.crt -caname server -passout pass:changeit
### Pitfall "the trustAnchors parameter must be non-empty" so we use keytool instead here
# openssl pkcs12 -export -in backendRootCA.crt -inkey backendRootCA.key -out backendRootCA.p12 -name serverCAcert -caname backendRootCA -passout pass:changeit
keytool -import -alias backendRootCA -file backendRootCA.crt -storetype PKCS12 -keystore backendRootCA.p12 -storepass changeit -noprompt

echo "generate client key file and client certificate signing request"
openssl genrsa -out client.key 2048
openssl req -new -key client.key -out client.csr --config clientCertificateSigningReq.conf

echo "Next sign the client certificate with your client RootCA"
openssl x509 -req -in client.csr -CA clientRootCA.crt -CAkey clientRootCA.key -CAcreateserial -out client.crt -days 365 -sha256

echo "convert X509 to PKCS12: client.p12 and clientRootCA.p12 certificate (for the Client-Component)"
openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12 -name clientcert -chain -CAfile clientRootCA.crt -caname client -passout pass:changeit
### Pitfall "the trustAnchors parameter must be non-empty" so we use keytool instead here
# openssl pkcs12 -export -in clientRootCA.crt -inkey clientRootCA.key -out clientRootCA.p12 -name clientCAcert -caname clientRootCA -passout pass:changeit
keytool -import -alias clientRootCA -file clientRootCA.crt -storetype PKCS12 -keystore clientRootCA.p12 -storepass changeit -noprompt

echo "Verschiebe die P12 als Keystores in die Resource Folder der Java Module als keystores"
mv clientRootCA.p12 ../Backend/src/main/resources
mv backend.p12 ../Backend/src/main/resources
mv backendRootCA.p12 ../Client/src/main/resources
mv client.p12 ../Client/src/main/resources
