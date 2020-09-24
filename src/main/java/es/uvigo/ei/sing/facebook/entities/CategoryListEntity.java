package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "category_list")
public class CategoryListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "external_id", nullable = false, unique = true, length = 50)
    private String externalId;
    @Basic
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(name = "page_has_category_list",
            joinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false))
    private Set<PageEntity> pages = new HashSet<>();
}
