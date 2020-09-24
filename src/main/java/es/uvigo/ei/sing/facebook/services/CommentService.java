package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.entities.CommentEntity;
import es.uvigo.ei.sing.facebook.repositories.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Set<CommentEntity> saveAll(Set<CommentEntity> commentEntities) {
        Iterable<CommentEntity> savedEntities = commentRepository.saveAll(commentEntities);
        return StreamSupport.stream(savedEntities.spliterator(), false).collect(Collectors.toSet());
    }

    public Optional<CommentEntity> findByExternalId(String externalId) {
        return commentRepository.findByExternalId(externalId);
    }
}
