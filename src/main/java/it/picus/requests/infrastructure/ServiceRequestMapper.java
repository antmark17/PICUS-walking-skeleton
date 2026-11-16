package it.picus.requests.infrastructure;

import it.picus.requests.domain.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
public class ServiceRequestMapper {
    public ServiceRequest toDomain(ServiceRequestEntity entity) {
        return new ServiceRequest(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getMisuraRichiesta(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public ServiceRequestEntity toEntity(ServiceRequest request) {
        return new ServiceRequestEntity(
                request.getId(),
                request.getOrganizationId(),
                request.getMisuraRichiesta(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt());
    }
}
