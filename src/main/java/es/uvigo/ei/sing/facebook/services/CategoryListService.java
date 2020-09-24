package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.entities.CategoryListEntity;
import es.uvigo.ei.sing.facebook.repositories.CategoryListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CategoryListService {

    private final CategoryListRepository categoryListRepository;

    @Autowired
    public CategoryListService(CategoryListRepository categoryListRepository) {
        this.categoryListRepository = categoryListRepository;
    }

    public CategoryListEntity save(CategoryListEntity categoryListEntity) {
        return categoryListRepository.save(categoryListEntity);
    }

    public Set<CategoryListEntity> saveAll(Set<CategoryListEntity> categoryLists) {
        Iterable<CategoryListEntity> savedEntities = categoryListRepository.saveAll(categoryLists);
        return StreamSupport.stream(savedEntities.spliterator(), false).collect(Collectors.toSet());
    }

    public Optional<CategoryListEntity> findByExternalId(String externalId) {
        return categoryListRepository.findByExternalId(externalId);
    }
}
