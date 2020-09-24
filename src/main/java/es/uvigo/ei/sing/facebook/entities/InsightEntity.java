package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "insights")
public class InsightEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "external_id", nullable = false, length = 50)
    private String externalId;
    @Basic
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Basic
    @Column(name = "inserted", nullable = false)
    private LocalDateTime inserted;
    @Basic
    @Column(name = "hash", nullable = false, unique = true)
    private String hash;
    @Basic
    @Column(name = "response", columnDefinition = "JSON")
    private String response;
}
