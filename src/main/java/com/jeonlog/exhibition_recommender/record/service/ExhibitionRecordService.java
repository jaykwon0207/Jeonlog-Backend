package com.jeonlog.exhibition_recommender.record.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.record.domain.MediaType;
import com.jeonlog.exhibition_recommender.record.domain.RecordMedia;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto.CreateRequest;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;

// ⬇️ 가중치 업데이트용 추가 import
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionRecordService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;
    private final UserGenreRepository userGenreRepository;

    @Transactional
    public Long addRecord(Long exhibitionId, User user, CreateRequest req) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시가 존재하지 않습니다."));

        if (req.getContent() != null && req.getContent().length() > 3000) {
            throw new IllegalArgumentException("content는 최대 3000자입니다.");
        }

        List<String> photos = req.getPhotoUrls() == null ? List.of() : req.getPhotoUrls();
        if (photos.size() > 10) {
            throw new IllegalArgumentException("사진은 최대 10장까지 업로드할 수 있습니다.");
        }

        String videoUrl = req.getVideoUrl();
        Integer videoDuration = req.getVideoDurationSeconds();
        if (videoUrl != null) {
            if (videoUrl.isBlank()) {
                throw new IllegalArgumentException("동영상 URL이 비어있습니다.");
            }
            if (videoDuration == null || videoDuration < 0 || videoDuration > 30) {
                throw new IllegalArgumentException("동영상 길이는 0~30초 사이여야 합니다.");
            }
        }

        ExhibitionRecord record = ExhibitionRecord.builder()
                .content(req.getContent())
                .likeCount(0L)
                .exhibition(exhibition)
                .user(user)
                .build();

        List<RecordMedia> mediaList = new ArrayList<>();
        for (String url : photos) {
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("비어있는 사진 URL이 포함되어 있습니다.");
            }
            mediaList.add(RecordMedia.builder()
                    .mediaType(MediaType.PHOTO)
                    .fileUrl(url)
                    .record(record)
                    .build());
        }

        if (videoUrl != null) {
            mediaList.add(RecordMedia.builder()
                    .mediaType(MediaType.VIDEO)
                    .fileUrl(videoUrl)
                    .thumbnailUrl(req.getVideoThumbnailUrl())
                    .durationSeconds(videoDuration)
                    .record(record)
                    .build());
        }

        record.getMediaList().addAll(mediaList);

        // 저장
        ExhibitionRecord saved = exhibitionRecordRepository.save(record);

        //전시기록 작성 가중치 +0.03
        UserGenre ug = userGenreRepository.findByUserId(user.getId())
                .orElseGet(() -> userGenreRepository.save(
                        UserGenre.builder().userId(user.getId()).build()
                ));
        ug.addFromExhibitionRecord(
                exhibition.getGenre(),
                exhibition.getExhibitionTheme()
        );

        return saved.getId();
    }

    @Transactional
    public void deleteRecord(Long exhibitionId, Long recordId, User user) {
        ExhibitionRecord record = exhibitionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시기록이 존재하지 않습니다."));

        if (!record.getExhibition().getId().equals(exhibitionId)) {
            throw new IllegalArgumentException("전시 정보가 일치하지 않습니다.");
        }

        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 전시기록만 삭제할 수 있습니다.");
        }

        var exhibition = record.getExhibition();
        userGenreRepository.findByUserId(user.getId()).ifPresent(ug -> {
            ug.revertExhibitionRecord(exhibition.getGenre(), exhibition.getExhibitionTheme()); // −0.03
        });

        exhibitionRecordRepository.delete(record);
    }

    @Transactional(readOnly = true)
    public List<ExhibitionRecordDto.MyRecordSummary> getMyRecords(User user) {
        List<ExhibitionRecord> records =
                exhibitionRecordRepository.findAllByUserOrderByCreatedAtDesc(user);

        return records.stream().map(r -> {
            var exhibition = r.getExhibition();
            var venue = exhibition != null ? exhibition.getVenue() : null;

            return ExhibitionRecordDto.MyRecordSummary.builder()
                    .id(r.getId())
                    .exhibitionId(exhibition != null ? exhibition.getId() : null)
                    .content(trim(r.getContent(), 200))
                    .likeCount(r.getLikeCount())
                    .createdAt(r.getCreatedAt())
                    .updatedAt(r.getUpdateAt())
                    .venueId(venue != null ? venue.getId() : null)
                    .venueName(venue != null ? venue.getName() : null)
                    .media(r.getMediaList().stream().map(m ->
                            ExhibitionRecordDto.MyRecordSummary.MediaItem.builder()
                                    .id(m.getId())
                                    .type(m.getMediaType())
                                    .fileUrl(m.getFileUrl())
                                    .thumbnailUrl(m.getThumbnailUrl())
                                    .durationSeconds(m.getDurationSeconds())
                                    .build()
                    ).toList())
                    .build();
        }).toList();
    }

    private String trim(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    @Transactional
    public Long updateRecord(Long exhibitionId, Long recordId, User user, ExhibitionRecordDto.UpdateRequest req) {
        ExhibitionRecord record = exhibitionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시기록이 존재하지 않습니다."));

        // 전시 ID 일치 검증
        if (!record.getExhibition().getId().equals(exhibitionId)) {
            throw new IllegalArgumentException("전시 정보가 일치하지 않습니다.");
        }

        // 소유자 검증
        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 전시기록만 수정할 수 있습니다.");
        }

        // 본문 검증/적용
        if (req.getContent() != null && req.getContent().length() > 3000) {
            throw new IllegalArgumentException("content는 최대 3000자입니다.");
        }
        record.setContentForUpdate(req.getContent());

        // 미디어 검증
        List<String> photos = req.getPhotoUrls() == null ? List.of() : req.getPhotoUrls();
        if (photos.size() > 10) {
            throw new IllegalArgumentException("사진은 최대 10장까지 업로드할 수 있습니다.");
        }

        String videoUrl = req.getVideoUrl();
        Integer videoDuration = req.getVideoDurationSeconds();
        if (videoUrl != null) {
            if (videoUrl.isBlank()) {
                throw new IllegalArgumentException("동영상 URL이 비어있습니다.");
            }
            if (videoDuration == null || videoDuration < 0 || videoDuration > 30) {
                throw new IllegalArgumentException("동영상 길이는 0~30초 사이여야 합니다.");
            }
        }

        // 미디어 전체 교체
        record.getMediaList().clear();

        // 새 미디어 채우기
        for (String url : photos) {
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("비어있는 사진 URL이 포함되어 있습니다.");
            }
            record.getMediaList().add(RecordMedia.builder()
                    .mediaType(MediaType.PHOTO)
                    .fileUrl(url)
                    .record(record)
                    .build());
        }

        if (videoUrl != null) {
            record.getMediaList().add(RecordMedia.builder()
                    .mediaType(MediaType.VIDEO)
                    .fileUrl(videoUrl)
                    .thumbnailUrl(req.getVideoThumbnailUrl())
                    .durationSeconds(videoDuration)
                    .record(record)
                    .build());
        }

        // likeCount는 PUT으로 수정하지 않음(좋아요 API로만 변경)
        ExhibitionRecord saved = exhibitionRecordRepository.save(record);
        return saved.getId();
    }
}
