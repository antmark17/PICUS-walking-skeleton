package it.picus.requests.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataServiceRequestRepository
        extends JpaRepository<ServiceRequestEntity, UUID> {
    Optional<ServiceRequestEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<ServiceRequestEntity> findByOrganizationId(UUID organizationId);
}
