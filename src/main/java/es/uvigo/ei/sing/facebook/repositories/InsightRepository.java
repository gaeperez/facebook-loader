package es.uvigo.ei.sing.facebook.repositories;

import es.uvigo.ei.sing.facebook.entities.InsightEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsightRepository extends CrudRepository<InsightEntity, Integer> {
    Optional<InsightEntity> findByHash(String hash);

    Optional<InsightEntity> findFirstByExternalIdOrderByCreatedDesc(String externalId);

    Optional<InsightEntity> findFirstByExternalIdOrderByCreatedAsc(String externalId);
}
