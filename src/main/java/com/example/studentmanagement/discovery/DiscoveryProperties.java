package com.example.studentmanagement.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "discovery")
public class DiscoveryProperties {

    private Map<String, List<String>> services = new LinkedHashMap<>();

    public Map<String, List<String>> getServices() {
        return services;
    }

    public void setServices(Map<String, List<String>> services) {
        this.services = services == null ? new LinkedHashMap<>() : services;
    }
}
