package es.uvigo.ei.sing.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "hour")
public class HourEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "day_of_week", nullable = false, length = 3)
    private String dayOfWeek;
    @Basic
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    @Basic
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @ManyToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false)
    private PageEntity page;
}
