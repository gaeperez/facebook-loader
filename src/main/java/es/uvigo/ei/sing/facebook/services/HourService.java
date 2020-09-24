package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.repositories.HourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HourService {

    private final HourRepository hourRepository;

    @Autowired
    public HourService(HourRepository hourRepository) {
        this.hourRepository = hourRepository;
    }
}
