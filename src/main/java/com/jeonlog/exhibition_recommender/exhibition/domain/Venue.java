package com.jeonlog.exhibition_recommender.exhibition.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VenueType type;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String phone;

    @Column(length = 50)
    private String email;

    @Column(length = 255)
    private String website;

    @Column(length = 100)
    private String openingHours;

    @Column(precision = 10, scale =6)
    private Double latitude;

    @Column(precision = 10, scale =6)
    private Double longitude;

    @Builder.Default
    @OneToMany(mappedBy = "venue")
    private List<Exhibition> exhibitions = new ArrayList<>();

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<VenuePhoto> photos = new ArrayList<>();



}
