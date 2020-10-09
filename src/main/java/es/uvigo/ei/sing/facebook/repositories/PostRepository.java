package es.uvigo.ei.sing.facebook.repositories;

import es.uvigo.ei.sing.facebook.entities.NodeEntity;
import es.uvigo.ei.sing.facebook.entities.PostEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PostRepository extends CrudRepository<PostEntity, Integer> {
    Optional<PostEntity> findFirstByOrderByCreatedAsc();

    Set<NodeEntity> findByPage_ExternalIdAndKeepUpdatingTrue(String pageId);

    Set<NodeEntity> findByPage_ExternalIdAndKeepUpdatingTrueOrParsedFalse(String pageId);

    Set<NodeEntity> findAllByPage_ExternalId(String pageId);

    Set<NodeEntity> findAllByParsedFalseAndPage_ExternalId(String pageId);
}
