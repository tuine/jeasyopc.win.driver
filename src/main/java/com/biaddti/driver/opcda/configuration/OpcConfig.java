package com.biaddti.driver.opcda.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "opc", ignoreInvalidFields = true)
@Data
public class OpcConfig {

    private String host;

    private String progId;

    private String groupJson;
}
