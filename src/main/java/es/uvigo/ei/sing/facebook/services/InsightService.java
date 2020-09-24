package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.entities.InsightEntity;
import es.uvigo.ei.sing.facebook.repositories.InsightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class InsightService {

    private final InsightRepository insightRepository;

    @Autowired
    public InsightService(InsightRepository insightRepository) {
        this.insightRepository = insightRepository;
    }

    public InsightEntity save(InsightEntity insightEntity) {
        return insightRepository.save(insightEntity);
    }

    public Set<InsightEntity> saveAll(Set<InsightEntity> insightEntities) {
        Iterable<InsightEntity> savedEntities = insightRepository.saveAll(insightEntities);
        return StreamSupport.stream(savedEntities.spliterator(), false).collect(Collectors.toSet());
    }

    public Optional<InsightEntity> findByHash(String hash) {
        return insightRepository.findByHash(hash);
    }

    public Optional<InsightEntity> findMostRecentInsight(String externalId) {
        return insightRepository.findFirstByExternalIdOrderByCreatedDesc(externalId);
    }

    public Optional<InsightEntity> findOldestInsight(String externalId) {
        return insightRepository.findFirstByExternalIdOrderByCreatedAsc(externalId);
    }
}
