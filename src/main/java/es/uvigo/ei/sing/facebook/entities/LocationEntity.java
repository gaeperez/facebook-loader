package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "location")
public class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "city", length = 100)
    private String city;
    @Basic
    @Column(name = "country", length = 100)
    private String country;
    @Basic
    @Column(name = "street")
    private String street;
    @Basic
    @Column(name = "latitude")
    private double latitude;
    @Basic
    @Column(name = "longitude")
    private double longitude;
    @Basic
    @Column(name = "zip", length = 50)
    private String zip;

    @OneToOne(mappedBy = "location")
    private PlaceEntity place;
}
