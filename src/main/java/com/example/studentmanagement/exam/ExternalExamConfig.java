package com.example.studentmanagement.exam;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ExternalExamProperties.class)
public class ExternalExamConfig {

    @Bean
    RestClient externalExamClient(ExternalExamProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return RestClient.builder().baseUrl(properties.getBaseUrl()).requestFactory(factory).build();
    }
}
