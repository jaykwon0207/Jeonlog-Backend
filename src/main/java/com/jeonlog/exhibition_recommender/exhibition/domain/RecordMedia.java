package com.jeonlog.exhibition_recommender.exhibition.domain;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "record_media")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecordMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType mediaType;  //미디어 타입(PHOTO/VIDEO)

    @Column(nullable = false)
    private String fileUrl;       //파일 URL

    @Column
    private String thumbnailUrl;  //동영상 썸네일(사진은 null)

    @Column
    private Integer durationSeconds;  //동영상 길이(초)(사진은 null)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    private ExhibitionRecord record;
}