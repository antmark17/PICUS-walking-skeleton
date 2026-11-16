package it.picus.requests.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import it.picus.requests.domain.RequestStatus;
import it.picus.requests.domain.ServiceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({JpaServiceRequestRepositoryAdapter.class, ServiceRequestMapper.class})
class JpaServiceRequestRepositoryAdapterTest {
    @Autowired JpaServiceRequestRepositoryAdapter repository;
    @Autowired TestEntityManager entityManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void savesAndReconstructsRequestWithoutResettingStateOrDates() {
        UUID id = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-10T09:30:00Z");
        Instant updatedAt = Instant.parse("2026-01-11T11:45:00Z");
        ServiceRequest original = new ServiceRequest(
                id,
                organizationId,
                "Accompagnamento al lavoro",
                RequestStatus.IN_ELABORAZIONE,
                createdAt,
                updatedAt);

        repository.save(original);
        entityManager.flush();
        entityManager.clear();

        ServiceRequest reloaded = repository.findById(id).orElseThrow();
        String storedStatus = jdbcTemplate.queryForObject(
                "select status from service_requests where id = ?",
                String.class,
                id);

        assertAll(
                () -> assertEquals(id, reloaded.getId()),
                () -> assertEquals(organizationId, reloaded.getOrganizationId()),
                () -> assertEquals("Accompagnamento al lavoro", reloaded.getMisuraRichiesta()),
                () -> assertEquals(RequestStatus.IN_ELABORAZIONE, reloaded.getStatus()),
                () -> assertEquals(createdAt, reloaded.getCreatedAt()),
                () -> assertEquals(updatedAt, reloaded.getUpdatedAt()),
                () -> assertEquals("IN_ELABORAZIONE", storedStatus));
    }

    @Test
    void filtersByOrganizationAndByIdAndOrganization() {
        UUID organizationA = UUID.randomUUID();
        UUID organizationB = UUID.randomUUID();
        ServiceRequest requestA = ServiceRequest.create(organizationA, "Misura A");
        ServiceRequest requestB = ServiceRequest.create(organizationB, "Misura B");
        repository.save(requestA);
        repository.save(requestB);
        entityManager.flush();
        entityManager.clear();

        List<ServiceRequest> organizationARequests = repository.findByOrganizationId(organizationA);

        assertAll(
                () -> assertEquals(1, organizationARequests.size()),
                () -> assertEquals(requestA.getId(), organizationARequests.getFirst().getId()),
                () -> assertTrue(repository.findByIdAndOrganizationId(
                        requestA.getId(), organizationA).isPresent()),
                () -> assertFalse(repository.findByIdAndOrganizationId(
                        requestA.getId(), organizationB).isPresent()),
                () -> assertEquals(2, repository.findAll().size()));
    }

    @Test
    void persistsDomainStateTransition() {
        ServiceRequest request = ServiceRequest.create(
                UUID.randomUUID(),
                "Accompagnamento al lavoro");
        repository.save(request);
        entityManager.flush();
        entityManager.clear();

        ServiceRequest persisted = repository.findById(request.getId()).orElseThrow();
        persisted.startProcessing();
        repository.save(persisted);
        entityManager.flush();
        entityManager.clear();

        ServiceRequest reloaded = repository.findById(request.getId()).orElseThrow();
        assertEquals(RequestStatus.IN_ELABORAZIONE, reloaded.getStatus());
    }

    @Test
    void thirtyPersistedSamplesContainOnlyWalkingSkeletonColumns() {
        for (int index = 0; index < 30; index++) {
            repository.save(ServiceRequest.create(
                    UUID.randomUUID(),
                    "Misura campione " + index));
        }
        entityManager.flush();

        Integer rowCount = jdbcTemplate.queryForObject(
                "select count(*) from service_requests",
                Integer.class);
        Set<String> columns = Set.copyOf(jdbcTemplate.queryForList(
                "select column_name from information_schema.columns "
                        + "where table_name = 'SERVICE_REQUESTS'",
                String.class));

        assertAll(
                () -> assertEquals(30, rowCount),
                () -> assertEquals(
                        Set.of(
                                "ID",
                                "ORGANIZATION_ID",
                                "MISURA_RICHIESTA",
                                "STATUS",
                                "CREATED_AT",
                                "UPDATED_AT"),
                        columns));
    }
}
