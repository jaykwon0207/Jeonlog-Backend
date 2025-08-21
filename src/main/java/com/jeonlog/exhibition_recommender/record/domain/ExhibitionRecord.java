package com.jeonlog.exhibition_recommender.record.domain;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.RecordMedia;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exhibition_records")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ExhibitionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 3000)
    private String content;  // 본문(최대 3000자)

    @Builder.Default
    @Column(nullable = false)
    private Long likeCount = 0L;    // 좋아요 수 기본 0

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;  // 생성 시각

    @LastModifiedDate
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;   // 수정 시각

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "list_index")  // 입력 순서 유지
    @Builder.Default
    private List<RecordMedia> mediaList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exhibition_id", nullable = false)
    private Exhibition exhibition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updateAt == null) updateAt = now;
        if (likeCount == null) likeCount = 0L;
    }

    @PreUpdate
    void onUpdate() {
        updateAt = LocalDateTime.now();
    }

    //좋아요 수 변경 메서드
    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
