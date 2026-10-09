package com.yourorg.librarybooking.zone;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "zones")
@Getter
@Setter
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    @Column
    private String imageUrl;
}
