package it.picus.requests.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import it.picus.requests.domain.ServiceRequest;
import it.picus.requests.domain.ServiceRequestRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaServiceRequestRepositoryAdapter implements ServiceRequestRepository {
    private final SpringDataServiceRequestRepository repository;
    private final ServiceRequestMapper mapper;

    public JpaServiceRequestRepositoryAdapter(
            SpringDataServiceRequestRepository repository,
            ServiceRequestMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ServiceRequest save(ServiceRequest request) {
        ServiceRequestEntity saved = repository.save(mapper.toEntity(request));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ServiceRequest> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ServiceRequest> findByIdAndOrganizationId(UUID id, UUID organizationId) {
        return repository.findByIdAndOrganizationId(id, organizationId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ServiceRequest> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ServiceRequest> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
