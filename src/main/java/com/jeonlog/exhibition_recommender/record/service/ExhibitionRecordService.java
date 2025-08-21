package com.jeonlog.exhibition_recommender.record.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.MediaType;
import com.jeonlog.exhibition_recommender.exhibition.domain.RecordMedia;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto.CreateRequest;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
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

        ExhibitionRecord saved = exhibitionRecordRepository.save(record);
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

        // 자식(mediaList)은 엔티티 매핑로 함께 제거
        exhibitionRecordRepository.delete(record);
    }

    @Transactional(readOnly = true)
    public List<ExhibitionRecordDto.MyRecordSummary> getMyRecords(User user) {
        List<ExhibitionRecord> records =
                exhibitionRecordRepository.findAllByUserOrderByCreatedAtDesc(user);

        return records.stream().map(r ->
                ExhibitionRecordDto.MyRecordSummary.builder()
                        .id(r.getId())
                        .exhibitionId(r.getExhibition().getId())
                        .content(trim(r.getContent(), 200))  // 본문 일부:200자
                        .likeCount(r.getLikeCount())
                        .createdAt(r.getCreatedAt())
                        .updatedAt(r.getUpdateAt())
                        .media(r.getMediaList().stream().map(m ->
                                ExhibitionRecordDto.MyRecordSummary.MediaItem.builder()
                                        .id(m.getId())
                                        .type(m.getMediaType())
                                        .fileUrl(m.getFileUrl())
                                        .thumbnailUrl(m.getThumbnailUrl())
                                        .durationSeconds(m.getDurationSeconds())
                                        .build()
                        ).toList())
                        .build()
        ).toList();
    }

    private String trim(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}


