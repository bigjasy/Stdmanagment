package com.example.studentmanagement.discovery;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguredServiceDirectoryTest {

    @Test
    void readsInstancesFromConfigurationAndDropsATrailingSlash() {
        DiscoveryProperties properties = new DiscoveryProperties();
        properties.setServices(Map.of("academic", List.of("http://localhost:8082/", "  http://localhost:8083  ", " ")));

        var instances = new ConfiguredServiceDirectory(properties).instances("academic");

        assertThat(instances).containsExactly(
                java.net.URI.create("http://localhost:8082"),
                java.net.URI.create("http://localhost:8083"));
    }
}
