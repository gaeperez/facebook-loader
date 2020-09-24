package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.repositories.CustomLabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomLabelService {

    private final CustomLabelRepository customLabelRepository;

    @Autowired
    public CustomLabelService(CustomLabelRepository customLabelRepository) {
        this.customLabelRepository = customLabelRepository;
    }
}
