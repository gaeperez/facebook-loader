package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.entities.PlaceEntity;
import es.uvigo.ei.sing.facebook.repositories.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    @Autowired
    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public PlaceEntity save(PlaceEntity placeEntity) {
        return placeRepository.save(placeEntity);
    }

    public void delete(PlaceEntity placeEntity) {
        placeRepository.delete(placeEntity);
    }

    public Optional<PlaceEntity> findByExternalId(String externalId) {
        return placeRepository.findByExternalId(externalId);
    }
}
