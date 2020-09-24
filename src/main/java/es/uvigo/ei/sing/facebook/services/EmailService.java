package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.entities.EmailEntity;
import es.uvigo.ei.sing.facebook.repositories.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class EmailService {

    private final EmailRepository emailRepository;

    @Autowired
    public EmailService(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public EmailEntity save(EmailEntity emailEntity) {
        return emailRepository.save(emailEntity);
    }

    public Set<EmailEntity> saveAll(Set<EmailEntity> emailEntities) {
        Iterable<EmailEntity> savedEntities = emailRepository.saveAll(emailEntities);
        return StreamSupport.stream(savedEntities.spliterator(), false).collect(Collectors.toSet());
    }

    public Optional<EmailEntity> findByName(String name) {
        return emailRepository.findByName(name);
    }

}
