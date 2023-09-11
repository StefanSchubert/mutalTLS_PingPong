/*
 * Copyright (c) 2023  by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 *
 */

package de.bluewhale.app.services;

import de.bluewhale.app.configs.ClientSslProperties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * <b>Notice:</b> You would normally place the SSlContext and Factories
 * in a separate WebClientConfig class which offers the @Bean WebClient
 */
@Service
@Slf4j
public class SampleService {

    @Autowired
    private ClientSslProperties sslProperties;

    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;

    private boolean lazyInitialized = false;

    private SslContext sslContext_with_client_cert = SslContextBuilder
            .forClient()
            .keyManager(this.keyManagerFactory)
            .trustManager(this.trustManagerFactory)
            .build();


    private SslContext sslContext_only_with_server_ca = SslContextBuilder
            .forClient()
            .trustManager(trustManagerFactory)
            .build();

    public SampleService() throws SSLException {
    }


    private void lazyInit() throws NoSuchAlgorithmException, KeyStoreException {

        if (!lazyInitialized) {

            this.keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] keyPassword = sslProperties.getKeyStorePassword().toCharArray();

            Resource resource = new ClassPathResource(sslProperties.getKeyStore());
            try (InputStream keyStoreData = resource.getInputStream()) {
                keyStore.load(keyStoreData, keyPassword);
                keyManagerFactory.init(keyStore, keyPassword);
            } catch (IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
                     KeyStoreException e) {
                e.printStackTrace();
            }

            this.trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore trustStore = KeyStore.getInstance("PKCS12");

            Resource resource2 = new ClassPathResource(sslProperties.getTrustStore());
            try (InputStream trustStoreData = resource.getInputStream()) {
                trustStore.load(trustStoreData, keyPassword);
                trustManagerFactory.init(trustStore);
            } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
                e.printStackTrace();
            }

            lazyInitialized = true;
        }

    }

    public String clientCertBackendCall() {

        try {
            lazyInit();
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error("Could not initzialice clients ssl context.",e);
            throw new RuntimeException(e);
        }

        WebClient webClient = WebClient.builder()
                .baseUrl("https://localhost:8443")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext_with_client_cert))))
                .build();

        String response = webClient.get()
                .uri("/pong")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return response;

    }

    public String plainBackendCall() {

        try {
            lazyInit();
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error("Could not initzialice clients ssl context.",e);
            throw new RuntimeException(e);
        }

        WebClient webClient = WebClient.builder()
                .baseUrl("https://localhost:8443")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext_only_with_server_ca))))
                .build();

        String response = webClient.get()
                .uri("/pong")
                .retrieve()
                .bodyToMono(String.class).block();

        return response;
    }
}
