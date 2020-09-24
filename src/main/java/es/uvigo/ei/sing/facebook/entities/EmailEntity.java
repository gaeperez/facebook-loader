package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "email")
public class EmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany
    @JoinTable(name = "page_has_email",
            joinColumns = @JoinColumn(name = "email_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false))
    private Set<PageEntity> pages = new HashSet<>();
}
