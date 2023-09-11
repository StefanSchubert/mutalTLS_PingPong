# Welcome to a round of mTLS Ping-Pong

This is just a small blueprint to demonstrate the configuration
of a **zero trust** approach for SpringBoot components.

If you try it out, you might encounter invalid certificates due to a past TTL.
In this case follow the instructions below to renew the certificates.

## Setup

This is the scenario you will find here, to play along with:

![ComponentView.png](ComponentView.png)

After starting both boot spring boot applications
you can observe what happens if you call

    http://localhost:8080/certPing

and 

    http://localhost:8080/normalPing

## Expected behavior

When using the /certPing endpoint, the client will use his client certificate 
to authenticate himself against the server. The caller will see the response of the /pong
call to the backend.

While using the /normalPing endpoint (or just trying to call /Pong directly by your self) 
you will encounter an error message, as you haven't been authorized.

### Notice

All I show here is the general authentication using mTLS. 
Authorization by means of ID the concrete client which is calling and checking
roles on it's ID is not covered by this small blueprint. 
However, you should know that you can add and read metadata to a clients certificate to do so.

## For reference: Creation process of the used certificates of this sample.

### RootCA for the backend (server) component

    openssl genrsa -out backendRootCA.key 2048
    openssl req -x509 -new -nodes -key backendRootCA.key -sha256 -days 1024 -out backendRootCA.crt

**Important**: Your CAs keyfile is a secret to be kept secure and should never be checked in.

The second call (creating your RootCA cert), ask a few things you may wish to adopt:

    openssl req -x509 -new -nodes -key backendRootCA.key -sha256 -days 1024 -out backendRootCA.crt
    You are about to be asked to enter information that will be incorporated
    into your certificate request.
    What you are about to enter is what is called a Distinguished Name or a DN.
    There are quite a few fields but you can leave some blank
    For some fields there will be a default value,
    If you enter '.', the field will be left blank.
    -----
    Country Name (2 letter code) [AU]:DE
    State or Province Name (full name) [Some-State]:Nordrhein-Westfalen             
    Locality Name (eg, city) []:Weilerswist
    Organization Name (eg, company) [Internet Widgits Pty Ltd]:private     
    Organizational Unit Name (eg, section) []:
    Common Name (e.g. server FQDN or YOUR name) []:localhost
    Email Address []:stefan.schubert@bluewhale.de


### Creating the Backend certificate and sign it with the backend rootCA

    openssl genrsa -out backend.key 2048
    openssl req -new -key backend.key -out backend.csr

**Important**: Your servers certificate keyfile is a secret to be kept secure and should never be checked in.

The second call (creating a signing request to be proccessed by the rootCA), ask a few things you may wish to adopt:

    openssl req -new -key backend.key -out backend.csr
    You are about to be asked to enter information that will be incorporated
    into your certificate request.
    What you are about to enter is what is called a Distinguished Name or a DN.
    There are quite a few fields but you can leave some blank
    For some fields there will be a default value,
    If you enter '.', the field will be left blank.
    -----
    Country Name (2 letter code) [AU]:DE
    State or Province Name (full name) [Some-State]:Nordrhein-Westfalen
    Locality Name (eg, city) []:Weilerswist
    Organization Name (eg, company) [Internet Widgits Pty Ltd]:private
    Organizational Unit Name (eg, section) []:
    Common Name (e.g. server FQDN or YOUR name) []:localhost
    Email Address []:stefan.schubert@bluewhale.de
    
    Please enter the following 'extra' attributes
    to be sent with your certificate request
    A challenge password []:pingpongMTLSdemo
    An optional company name []:

Next sign the backend server certificate with your RootCA

    openssl x509 -req -in backend.csr -CA backendRootCA.crt -CAkey backendRootCA.key -CAcreateserial -out backend.crt -days 1024 -sha256

### Creation of the rootCA used by the client component

    openssl genrsa -out clientRootCA.key 2048
    openssl req -x509 -new -nodes -key clientRootCA.key -sha256 -days 1024 -out clientRootCA.crt

### Creating the client certificate and sign it with the client rootCA

    openssl genrsa -out client.key 2048
    openssl req -new -key client.key -out client.csr
    openssl x509 -req -in client.csr -CA clientRootCA.crt -CAkey clientRootCA.key -CAcreateserial -out client.crt -days 1024 -sha256

## Converting the Certificates into PKCS#12 standard

The .crt are X.509 certificates in PEM-Format.
The .p12 (or .pfx) serves the storage of 
multiple certificates, as well as the certificates chain (i.e.) 
all signing certificates as well.

### Client.p12 and clientRootCA.p12 certificate
    
    openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12 -name clientcert -CAfile clientRootCA.crt -caname client
    openssl pkcs12 -export -in clientRootCA.crt -inkey clientRootCA.key -out clientRootCA.p12 -name clientCAcert -caname rootCA

### backend.p12 and backendRootCA.p12 certificate (for the Server-Component)

    openssl pkcs12 -export -in backend.crt -inkey backend.key -out backend.p12 -name servercert -CAfile backendRootCA.crt -caname server
    openssl pkcs12 -export -in backendRootCA.crt -inkey backendRootCA.key -out backendRootCA.p12 -name serverCAcert -caname rootCA

### Pitfall "the trustAnchors parameter must be non-empty"

OpenSSL does not automatically tag your rootCA as "trustedCertEntry", that why java does not
recognize you cert in the truststore.
To solve this issue create the truststore directly via the keytool, or add your cert into the truststore via the keytool
again, like this:

    keytool -import -alias clientCA -file clientRootCA.crt -storetype PKCS12 -keystore clientRootCA.p12

I had to use a different aliasname an confirm, that I wanted to add the certificate a second time into
the same store. You will need to repeat this step with the backendRootCA as well.

## BestPractise Thoughts

Having a look at this zero trust architecture.
What might be better in an environment with tens, e.g. 50 of clients?

* Would you import each client certificate and restart your server each time a new client component joins in?
* Would you instead take care that each client certificate is signed by the same CA?
* What about the need of removing a bad or compromised client?
* How about delegating the checking on client certs to an IAM broker? Could this one integrated in spring boot?

## Credits

Goes to chatGPT (model4) which I asked how to build this scenario (required spring configuration) and which concreate openssl
commands do I need to create the required certificates.