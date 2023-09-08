# Welcome to a round of mTLS Ping-Pong

This is just a small blueprint to demonstrate the configuration
of a **zero trust** approach for SpringBoot components.

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

### Creating the Backend certificate and sign it with the backend rootCA

    openssl genrsa -out backend.key 2048
    openssl req -new -key backend.key -out backend.csr
    openssl x509 -req -in backend.csr -CA backendRootCA.crt -CAkey backendRootCA.key -CAcreateserial -out backend.crt -days 1024 -sha256

### Creation of the rootCA used by the client component

    openssl genrsa -out clientRootCA.key 2048
    openssl req -x509 -new -nodes -key clientRootCA.key -sha256 -days 1024 -out clientRootCA.crt

### Creating the client certificate and sign it with the client rootCA

    openssl genrsa -out client.key 2048
    openssl req -new -key client.key -out client.csr
    openssl x509 -req -in client.csr -CA clientRootCA.crt -CAkey clientRootCA.key -CAcreateserial -out client.crt -days 1024 -sha256

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