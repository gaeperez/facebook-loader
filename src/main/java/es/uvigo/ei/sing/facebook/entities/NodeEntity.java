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
@Table(name = "node")
@Inheritance(strategy = InheritanceType.JOINED)
public class NodeEntity {
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
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    @Basic
    @Column(name = "keep_updating", nullable = false)
    private boolean keepUpdating;
    @Basic
    @Column(name = "parsed", nullable = false)
    private boolean parsed;
    @Basic
    @Column(name = "perma_link", length = 500)
    private String permaLink;

    @ManyToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false)
    private PageEntity page;

    @ManyToMany
    @JoinTable(name = "node_has_comment",
            joinColumns = @JoinColumn(name = "node_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "comment_id", referencedColumnName = "id", nullable = false))
    private Set<CommentEntity> comments = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "place_id", referencedColumnName = "id")
    private PlaceEntity place;
}
