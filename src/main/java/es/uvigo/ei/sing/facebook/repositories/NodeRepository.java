package es.uvigo.ei.sing.facebook.repositories;

import es.uvigo.ei.sing.facebook.entities.NodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeRepository extends CrudRepository<NodeEntity, Integer> {
    Optional<NodeEntity> findByExternalId(String externalId);
}
