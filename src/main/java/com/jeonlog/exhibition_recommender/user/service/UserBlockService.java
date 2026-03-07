package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.UserBlock;
import com.jeonlog.exhibition_recommender.user.dto.BlockActionResponse;
import com.jeonlog.exhibition_recommender.user.dto.BlockedUserDto;
import com.jeonlog.exhibition_recommender.user.repository.FollowRepository;
import com.jeonlog.exhibition_recommender.user.repository.UserBlockRepository;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final FollowRepository followRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;
    private final DiscordUserBlockWebhookService discordUserBlockWebhookService;

    @Transactional
    public BlockActionResponse block(User blocker, Long blockedUserId) {
        User blocked = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new IllegalArgumentException("차단할 사용자를 찾을 수 없습니다."));

        if (blocker.getId().equals(blocked.getId())) {
            throw new IllegalArgumentException("자기 자신은 차단할 수 없습니다.");
        }

        if (!userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            userBlockRepository.save(new UserBlock(blocker, blocked));

            // 차단 시 기존 팔로우 관계는 즉시 정리
            followRepository.deleteByFollowerAndFollowing(blocker, blocked);
            followRepository.deleteByFollowerAndFollowing(blocked, blocker);

            discordUserBlockWebhookService.sendUserBlocked(blocker, blocked);
        }

        return BlockActionResponse.builder()
                .blockedUserId(blocked.getId())
                .blockedUserNickname(blocked.getNickname())
                .blocked(true)
                .hiddenRecordIds(exhibitionRecordRepository.findIdsByUser(blocked))
                .build();
    }

    @Transactional
    public BlockActionResponse unblock(User blocker, Long blockedUserId) {
        User blocked = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new IllegalArgumentException("차단 해제할 사용자를 찾을 수 없습니다."));

        userBlockRepository.deleteByBlockerAndBlocked(blocker, blocked);

        return BlockActionResponse.builder()
                .blockedUserId(blocked.getId())
                .blockedUserNickname(blocked.getNickname())
                .blocked(false)
                .hiddenRecordIds(List.of())
                .build();
    }

    public List<BlockedUserDto> getBlockedUsers(User blocker) {
        return userBlockRepository.findByBlockerWithBlocked(blocker)
                .stream()
                .map(block -> BlockedUserDto.of(block.getBlocked(), block.getCreatedAt()))
                .toList();
    }
}
