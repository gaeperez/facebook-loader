package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "comment")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "external_id", nullable = false, unique = true, length = 50)
    private String externalId;
    @Basic
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Basic
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    @Basic
    @Column(name = "like_count")
    private long likeCount;
    @Basic
    @Column(name = "comment_count")
    private long commentCount;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "attachment_id", referencedColumnName = "id")
    private AttachmentEntity attachment;

    // https://viralpatel.net/blogs/hibernate-self-join-annotations-one-to-many-mapping/
    // Comment replies
    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private CommentEntity parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentEntity> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "node_has_comment",
            joinColumns = @JoinColumn(name = "comment_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "node_id", referencedColumnName = "id", nullable = false))
    private Set<NodeEntity> nodes = new HashSet<>();
}
