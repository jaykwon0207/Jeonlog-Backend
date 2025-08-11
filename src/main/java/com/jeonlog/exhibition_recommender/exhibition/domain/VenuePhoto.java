package com.jeonlog.exhibition_recommender.exhibition.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue_photos",
        indexes = {
                @Index(name = "idx_venue_photos_venue_id", columnList = "venue_id"),
                @Index(name = "idx_venue_photos_sort", columnList = "venue_id, sort_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_venue_sort", columnNames = {"venue_id", "sort_order"})
        })
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class VenuePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //어떤 Venue 소속인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;


    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 200)
    private String caption;

    @Column(name = "sort_order",nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    @Builder.Default
    private boolean isCover = false;


}
