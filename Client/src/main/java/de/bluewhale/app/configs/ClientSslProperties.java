/*
 * Copyright (c) 2023  by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 *
 */

package de.bluewhale.app.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * WebFlux Client won't be configured
 * through Sprin Boots standard server:ssl: configs
 * we have a own client:ssl: config which will be read.
 *
 * @author Stefan Schubert
 */
@Configuration
@ConfigurationProperties(prefix = "client.ssl")
@Data
public class ClientSslProperties {

    private String trustStore;
    private String trustStorePassword;
    private String trustStoreType;
    private String keyStore;
    private String keyStoreType;
    private String keyStorePassword;

}
