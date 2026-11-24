package com.example.studentmanagement.discovery;

import java.net.URI;
import java.util.List;

/**
 * Looks up a service by its logical name. Callers never see a host or a port.
 */
public interface ServiceDirectory {

    List<URI> instances(String serviceName);
}
