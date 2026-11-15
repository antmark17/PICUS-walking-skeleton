package it.picus.requests.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta di persistenza. L'adapter JPA implementato dal Gruppo 2 realizza questa interfaccia. */
public interface ServiceRequestRepository {
    ServiceRequest save(ServiceRequest request);
    Optional<ServiceRequest> findById(UUID id);
    Optional<ServiceRequest> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<ServiceRequest> findByOrganizationId(UUID organizationId);
    List<ServiceRequest> findAll();
}
