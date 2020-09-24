package es.uvigo.ei.sing.facebook.repositories;

import es.uvigo.ei.sing.facebook.entities.CategoryListEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryListRepository extends CrudRepository<CategoryListEntity, Integer> {
    Optional<CategoryListEntity> findByExternalId(String externalId);
}
