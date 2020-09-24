package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.entities.PageEntity;
import es.uvigo.ei.sing.facebook.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PageService {

    private final PageRepository pageRepository;

    @Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public PageEntity save(PageEntity pageEntity) {
        return pageRepository.save(pageEntity);
    }

    public void delete(PageEntity pageEntity) {
        pageRepository.delete(pageEntity);
    }

    public Optional<PageEntity> findByExternalId(String externalId) {
        return pageRepository.findByExternalId(externalId);
    }
}
