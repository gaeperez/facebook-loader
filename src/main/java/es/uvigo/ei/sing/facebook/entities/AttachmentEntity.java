package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "attachment")
public class AttachmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Basic
    @Column(name = "media_type", length = 50)
    private String mediaType;
    @Basic
    @Column(name = "title", length = 500)
    private String title;
    @Basic
    @Column(name = "type", length = 100, nullable = false)
    private String type;
    @Basic
    @Column(name = "url", length = 1000, nullable = false)
    private String url;

    @OneToOne(mappedBy = "attachment")
    private CommentEntity comment;
}
