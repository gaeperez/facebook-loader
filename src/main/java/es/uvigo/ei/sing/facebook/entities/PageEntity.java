package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "page")
public class PageEntity {
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
    @Basic
    @Column(name = "link", nullable = false)
    private String link;
    @Basic
    @Column(name = "about", nullable = false)
    private String about;
    @Basic
    @Column(name = "parsed", nullable = false)
    private boolean isParsed;
    @Basic
    @Column(name = "checkins")
    private int checkins;
    @Basic
    @Column(name = "category")
    private String category;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Basic
    @Column(name = "engagement")
    private long engagement;
    @Basic
    @Column(name = "general_info", columnDefinition = "TEXT")
    private String generalInfo;
    @Basic
    @Column(name = "impressum", length = 2000)
    private String impressum;
    @Basic
    @Column(name = "phone", length = 80)
    private String phone;
    @Basic
    @Column(name = "website")
    private String website;
    @Basic
    @Column(name = "single_line_address", length = 300)
    private String singleLineAddress;
    @Basic
    @Column(name = "products", length = 500)
    private String products;
    @Basic
    @Column(name = "price_range", length = 10)
    private String priceRange;
    @Basic
    @Column(name = "overall_star_rating")
    private double overallStarRating;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HourEntity> hours = new HashSet<>();
    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NodeEntity> nodes = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "page_has_category_list",
            joinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false))
    private Set<CategoryListEntity> categoryLists = new HashSet<>();
    @ManyToMany
    @JoinTable(name = "page_has_email",
            joinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "email_id", referencedColumnName = "id", nullable = false))
    private Set<EmailEntity> emails = new HashSet<>();
}
