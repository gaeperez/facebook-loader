package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "post")
public class PostEntity extends NodeEntity {
    @Basic
    @Column(name = "link", length = 2000)
    private String link;
    @Basic
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    @Basic
    @Column(name = "status_type", length = 100)
    private String statusType;
    @Basic
    @Column(name = "story", columnDefinition = "TEXT")
    private String story;
    @Basic
    @Column(name = "shares")
    private Long shares;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MessageTagEntity> messageTags = new HashSet<>();
}
