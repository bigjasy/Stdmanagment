package com.example.studentmanagement.discovery;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfiguredServiceDirectory implements ServiceDirectory {

    private final DiscoveryProperties properties;

    public ConfiguredServiceDirectory(DiscoveryProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<URI> instances(String serviceName) {
        List<String> configured = properties.getServices().getOrDefault(serviceName, List.of());
        List<URI> instances = new ArrayList<>();
        for (String raw : configured) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            instances.add(URI.create(stripTrailingSlash(raw.trim())));
        }
        return List.copyOf(instances);
    }

    private static String stripTrailingSlash(String value) {
        String normalized = value;
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
