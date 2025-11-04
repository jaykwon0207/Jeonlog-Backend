package com.jeonlog.exhibition_recommender.scrap.domain;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "record_scraps",
        uniqueConstraints = @UniqueConstraint(name = "uk_record_scrap_record_user", columnNames = {"record_id", "user_id"}),
        indexes = {
                @Index(name = "idx_record_scrap_user", columnList = "user_id"),
                @Index(name = "idx_record_scrap_record", columnList = "record_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecordScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_record_scrap_record"))
    private ExhibitionRecord record;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_record_scrap_user"))
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime scrappedAt;
}
