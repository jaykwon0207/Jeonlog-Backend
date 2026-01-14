package com.jeonlog.exhibition_recommender.record.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.record.domain.Hashtag;
import com.jeonlog.exhibition_recommender.record.domain.MediaType;
import com.jeonlog.exhibition_recommender.record.domain.RecordMedia;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto.CreateRequest;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.dto.RecordSearchCondition;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.record.repository.HashtagRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;

// ⬇️ 가중치 업데이트용 추가 import
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;

import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitionRecordService {

    private final UserRepository userRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;
    private final UserGenreRepository userGenreRepository;
    private final HashtagRepository hashtagRepository;

    @Transactional
    public Long addRecord(Long exhibitionId, User user, CreateRequest req) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시가 존재하지 않습니다."));

        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("title은 필수입니다.");
        }
        if (req.getTitle().length() > 100) {
            throw new IllegalArgumentException("title은 최대 100자입니다.");
        }

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
                .title(req.getTitle())
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

        // 본문(content)에서 해시태그 이름(Set<String>) 파싱
//      Set<String> tagNames = parseHashtags(req.getContent());
        Set<String> tagNames = new HashSet<>(parseHashtags(req.getContent()));
        tagNames.addAll(normalizeHashtags(req.getHashtags()));

        Set<Hashtag> hashtagEntities = findOrCreateHashtags(tagNames);
        record.updateHashtags(hashtagEntities);


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

    private Set<String> normalizeHashtags(Set<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) return Collections.emptySet();

        return rawTags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .map(t -> t.startsWith("#") ? t.substring(1) : t)
                .filter(t -> !t.isBlank())
                .collect(Collectors.toSet());
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
                    .title(r.getTitle())
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

        // title 검증/적용
        if (req.getTitle() != null) {
            if (req.getTitle().isBlank()) {
                throw new IllegalArgumentException("title이 비어있습니다.");
            }
            if (req.getTitle().length() > 100) {
                throw new IllegalArgumentException("title은 최대 100자입니다.");
            }
            record.setTitleForUpdate(req.getTitle());
        }

        // 본문 검증/적용
        if (req.getContent() != null && req.getContent().length() > 3000) {
            throw new IllegalArgumentException("content는 최대 3000자입니다.");
        }
        record.setContentForUpdate(req.getContent());

        // 해시태그 업데이트 로직 추가
        //Set<String> tagNames = parseHashtags(req.getContent());
        Set<String> tagNames = new HashSet<>(parseHashtags(req.getContent()));
        tagNames.addAll(normalizeHashtags(req.getHashtags()));
        Set<Hashtag> hashtagEntities = findOrCreateHashtags(tagNames);
        record.updateHashtags(hashtagEntities);



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

    //전시기록 상세조회
    @Transactional(readOnly = true)
    public ExhibitionRecordDto.RecordDetailResponse getRecordDetail(Long recordId) {
        ExhibitionRecord record = exhibitionRecordRepository.findWithDetailById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시기록이 존재하지 않습니다."));

        var exhibition = record.getExhibition();
        var venue = (exhibition != null) ? exhibition.getVenue() : null;

        List<ExhibitionRecordDto.RecordMediaDto> mediaDtoList = record.getMediaList().stream()
                .map(ExhibitionRecordDto.RecordMediaDto::new)
                .toList();

        Set<String> hashtags = record.getHashtags().stream()
                .map(Hashtag::getName)
                .collect(Collectors.toSet());

        return ExhibitionRecordDto.RecordDetailResponse.builder()
                .recordId(record.getId())
                .title(record.getTitle())
                .content(record.getContent())
                .likeCount(record.getLikeCount())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdateAt())

                .writerId(record.getUser().getId())
                .writerNickname(record.getUser().getNickname())
                .writerProfileImgUrl(record.getUser().getProfileImageUrl())

                .exhibitionId(exhibition != null ? exhibition.getId() : null)
                .exhibitionTitle(exhibition != null ? exhibition.getTitle() : null)

                .venueId(venue != null ? venue.getId() : null)
                .venueName(venue != null ? venue.getName() : null)

                .mediaList(mediaDtoList)
                .hashtags(hashtags)
                .build();
    }


    @Transactional(readOnly = true)
    public Page<ExhibitionRecordDto.RecordListResponse> getAllRecords(Pageable pageable) {

        // 1. Repository에서 전체 데이터 조회 (Page<ExhibitionRecord> 반환)
        // JPA 기본 메서드인 findAll()을 사용합니다.
        Page<ExhibitionRecord> recordsPage = exhibitionRecordRepository.findAll(pageable);

        // 2. Page<ExhibitionRecord> -> Page<RecordListResponse> DTO로 변환
        // (getRecordsByExhibition 메서드의 변환 로직과 완전히 동일합니다)
        return recordsPage.map(record -> {

            // 미디어 리스트를 DTO 리스트로 변환
            List<ExhibitionRecordDto.RecordMediaDto> mediaDtoList = record.getMediaList().stream()
                    .map(ExhibitionRecordDto.RecordMediaDto::new) // RecordMediaDto 생성자 활용
                    .toList();

            return ExhibitionRecordDto.RecordListResponse.builder()
                    .recordId(record.getId())
                    .title(record.getTitle())
                    .content(record.getContent())
                    .likeCount(record.getLikeCount())
                    .createdAt(record.getCreatedAt())
                    .writerNickname(record.getUser().getNickname())
                    .writerProfileImgUrl(record.getUser().getProfileImageUrl())
                    .mediaList(mediaDtoList)
                    .build();
        });
    }



    // 본문(content)에서 #해시태그 를 파싱하여 Set<String>으로 반환
    private Set<String> parseHashtags(String content) {
        if (content == null) {
            return Collections.emptySet();
        }

        // 정규표현식으로 #단어 추출
        Pattern pattern = Pattern.compile("#([\\w가-힣]+)");
        Matcher matcher = pattern.matcher(content);
        Set<String> tags = new HashSet<>();
        while (matcher.find()) {
            tags.add(matcher.group(1)); // # 제외한 단어 (예: "사랑")
        }
        return tags;
    }


    // 해시태그 이름 목록(Set<String>)을 받아,
    // DB에서 조회하거나(Find) 새로 생성하여(Create) 엔티티 Set<Hashtag>으로 반환
    private Set<Hashtag> findOrCreateHashtags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        // 이미 존재하는 태그들을 DB에서 한 번에 조회
        Set<Hashtag> existingTags = hashtagRepository.findByNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Hashtag::getName)
                .collect(Collectors.toSet());

        // DB에 존재하지 않는 태그 이름들만 필터링
        Set<String> newTagNames = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .collect(Collectors.toSet());

        // 새 태그 엔티티 생성 및 저장
        Set<Hashtag> newTags = new HashSet<>();
        for (String newName : newTagNames) {
            Hashtag savedTag = hashtagRepository.save(new Hashtag(newName));
            newTags.add(savedTag);
        }

        // 기존 태그 + 새로 저장된 태그 모두 반환
        existingTags.addAll(newTags);
        return existingTags;
    }

    @Transactional(readOnly = true)
    public Page<ExhibitionRecordDto.RecordListResponse> searchRecords(
            String query, Pageable pageable) {

        Page<ExhibitionRecord> recordsPage = exhibitionRecordRepository.search(query, pageable);

        return recordsPage.map(record -> {
            List<ExhibitionRecordDto.RecordMediaDto> mediaDtoList = record.getMediaList().stream()
                    .map(ExhibitionRecordDto.RecordMediaDto::new)
                    .toList();

            Set<String> hashtags = record.getHashtags().stream()
                    .map(Hashtag::getName)
                    .collect(Collectors.toSet());

            return ExhibitionRecordDto.RecordListResponse.builder()
                    .recordId(record.getId())
                    .title(record.getTitle())
                    .content(record.getContent())
                    .likeCount(record.getLikeCount())
                    .createdAt(record.getCreatedAt())
                    .writerNickname(record.getUser().getNickname())
                    .writerProfileImgUrl(record.getUser().getProfileImageUrl())
                    .mediaList(mediaDtoList)
                    .hashtags(hashtags) 
                    .exhibitionTitle(record.getExhibition().getTitle())
                    .build();
        });
    }


    @Transactional(readOnly = true)
    public Page<ExhibitionRecordDto.RecordListResponse> getUserRecords(Long userId, Pageable pageable) {

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("대상 유저 없음"));

        Page<ExhibitionRecord> recordsPage =
                exhibitionRecordRepository.findByUserOrderByCreatedAtDesc(target, pageable);

        return recordsPage.map(record -> {
            List<ExhibitionRecordDto.RecordMediaDto> mediaDtoList = record.getMediaList().stream()
                    .map(ExhibitionRecordDto.RecordMediaDto::new)
                    .toList();

            Set<String> hashtags = record.getHashtags().stream()
                    .map(Hashtag::getName)
                    .collect(Collectors.toSet());

            return ExhibitionRecordDto.RecordListResponse.builder()
                    .recordId(record.getId())
                    .title(record.getTitle())
                    .content(record.getContent())
                    .likeCount(record.getLikeCount())
                    .createdAt(record.getCreatedAt())
                    .writerNickname(record.getUser().getNickname())
                    .writerProfileImgUrl(record.getUser().getProfileImageUrl())
                    .mediaList(mediaDtoList)
                    .hashtags(hashtags)
                    .exhibitionTitle(record.getExhibition() != null ? record.getExhibition().getTitle() : null)
                    .build();
        });
    }

}
