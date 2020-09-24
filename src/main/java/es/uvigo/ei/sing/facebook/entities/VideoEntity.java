package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "video")
public class VideoEntity extends NodeEntity {
    @Basic
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    @Basic
    @Column(name = "source", nullable = false, length = 2000)
    private String source;
    @Basic
    @Column(name = "length", nullable = false)
    private Double length;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "video")
    private Set<CustomLabelEntity> customLabels;
}
