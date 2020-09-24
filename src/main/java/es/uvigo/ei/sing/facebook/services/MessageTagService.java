package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.repositories.MessageTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageTagService {

    private final MessageTagRepository messageTagRepository;

    @Autowired
    public MessageTagService(MessageTagRepository messageTagRepository) {
        this.messageTagRepository = messageTagRepository;
    }
}
