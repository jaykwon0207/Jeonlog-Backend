package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.user.domain.Follow;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.SimpleUserProfileDto;
import com.jeonlog.exhibition_recommender.user.exception.UserNotFoundException;
import com.jeonlog.exhibition_recommender.user.repository.FollowRepository;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;
    private final NotificationService notificationService;


    // 🔹 팔로잉 목록
    public List<SimpleUserProfileDto> getFollowings(String myEmail) {
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));

        List<Follow> followings = followRepository.findByFollower(me);

        return followings.stream()
                .map(relation -> {
                    User target = relation.getFollowing();
                    boolean isFollowing = true;
                    int postCount = exhibitionRecordRepository.countByUser(target);
                    int followerCount = followRepository.countByFollowing(target);
                    int followingCount = followRepository.countByFollower(target);
                    return SimpleUserProfileDto.from(target, isFollowing, postCount, followerCount, followingCount);
                })
                .toList();
    }

    // 🔹 팔로워 목록
    public List<SimpleUserProfileDto> getFollowers(String myEmail) {
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));

        List<Follow> followers = followRepository.findByFollowing(me);

        return followers.stream()
                .map(relation -> {
                    User target = relation.getFollower();
                    boolean isFollowing = followRepository.existsByFollowerAndFollowing(me, target);
                    int postCount = exhibitionRecordRepository.countByUser(target);
                    int followerCount = followRepository.countByFollowing(target);
                    int followingCount = followRepository.countByFollower(target);
                    return SimpleUserProfileDto.from(target, isFollowing, postCount, followerCount, followingCount);
                })
                .toList();
    }

    // 🔹 팔로우
    @Transactional
    public void follow(String email, Long targetId) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("팔로우 대상 유저 없음"));

        if (me.equals(target)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }

        // 이미 팔로우면 그냥 끝 (알림도 안 보냄)
        if (followRepository.existsByFollowerAndFollowing(me, target)) {
            return;
        }

        followRepository.save(new Follow(me, target));

        // 팔로우 알림 생성 + 푸시
        notificationService.notifyFollow(
                target.getId(),
                me.getId(),
                me.getNickname()
        );
    }


    // 🔹 언팔로우
    @Transactional
    public void unfollow(String email, Long targetId) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("언팔 대상 유저 없음"));

        followRepository.deleteByFollowerAndFollowing(me, target);
    }

    // 🔹 다른 유저 프로필 조회
    public SimpleUserProfileDto getUserProfile(String myEmail, Long targetUserId) {
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("대상 유저 없음"));

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(me, target);
        int postCount = exhibitionRecordRepository.countByUser(target);
        int followerCount = followRepository.countByFollowing(target);
        int followingCount = followRepository.countByFollower(target);

        return SimpleUserProfileDto.from(target, isFollowing, postCount, followerCount, followingCount);
    }
}