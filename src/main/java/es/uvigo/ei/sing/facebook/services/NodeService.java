package es.uvigo.ei.sing.facebook.services;

import es.uvigo.ei.sing.facebook.entities.NodeEntity;
import es.uvigo.ei.sing.facebook.entities.PostEntity;
import es.uvigo.ei.sing.facebook.entities.VideoEntity;
import es.uvigo.ei.sing.facebook.repositories.NodeRepository;
import es.uvigo.ei.sing.facebook.repositories.PostRepository;
import es.uvigo.ei.sing.facebook.repositories.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class NodeService {

    private final NodeRepository nodeRepository;
    private final PostRepository postRepository;
    private final VideoRepository videoRepository;

    @Autowired
    public NodeService(NodeRepository nodeRepository, PostRepository postRepository, VideoRepository videoRepository) {
        this.nodeRepository = nodeRepository;
        this.postRepository = postRepository;
        this.videoRepository = videoRepository;
    }

    public NodeEntity save(NodeEntity nodeEntity) {
        return nodeRepository.save(nodeEntity);
    }

    public Set<NodeEntity> saveAll(Set<NodeEntity> nodeEntities) {
        Iterable<NodeEntity> savedEntities = nodeRepository.saveAll(nodeEntities);
        return StreamSupport.stream(savedEntities.spliterator(), false).collect(Collectors.toSet());
    }

    public Set<NodeEntity> findAllPosts(String pageId) {
        return postRepository.findAllByPage_ExternalId(pageId);
    }

    public Set<NodeEntity> findByKeepUpdatingPosts(String pageId) {
        return postRepository.findByPage_ExternalIdAndKeepUpdatingTrue(pageId);
    }

    public Set<NodeEntity> findByKeepUpdatingOrNotParsedPosts(String pageId) {
        return postRepository.findByPage_ExternalIdAndKeepUpdatingTrueOrParsedFalse(pageId);
    }

    public Set<NodeEntity> findNotParsedPosts(String pageId) {
        return postRepository.findAllByParsedFalseAndPage_ExternalId(pageId);
    }

    public Set<NodeEntity> findAllVideos(String pageId) {
        return videoRepository.findAllByPage_ExternalId(pageId);
    }

    public Set<NodeEntity> findByKeepUpdatingVideos(String pageId) {
        return videoRepository.findByPage_ExternalIdAndKeepUpdatingTrue(pageId);
    }

    public Set<NodeEntity> findByKeepUpdatingVideosOrNotParsed(String pageId) {
        return videoRepository.findByPage_ExternalIdAndKeepUpdatingTrueOrParsedFalse(pageId);
    }

    public Set<NodeEntity> findNotParsedVideos(String externalId) {
        return videoRepository.findAllByParsedFalseAndPage_ExternalId(externalId);
    }

    public Optional<NodeEntity> findByExternalId(String externalId) {
        return nodeRepository.findByExternalId(externalId);
    }

    public Optional<PostEntity> findOldestPost() {
        return postRepository.findFirstByOrderByCreatedAsc();
    }

    public Optional<VideoEntity> findOldestVideo() {
        return videoRepository.findFirstByOrderByCreatedAsc();
    }
}
