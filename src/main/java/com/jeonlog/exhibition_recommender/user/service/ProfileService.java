package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;
    private final NotificationService notificationService;


    // 내 팔로잉 목록
    public List<SimpleUserProfileDto> getFollowings(String myEmail) {
        User me = getUserByEmail(myEmail);

        List<Follow> followings = followRepository.findByFollower(me);

        return followings.stream()
                .map(relation -> toDto(
                        relation.getFollowing(),
                        true
                ))
                .toList();
    }

    // 내 팔로워 목록
    public List<SimpleUserProfileDto> getFollowers(String myEmail) {
        User me = getUserByEmail(myEmail);

        List<Long> myFollowingIds = followRepository.findFollowingIdsByFollower(me);
        List<Follow> followers = followRepository.findByFollowing(me);

        return followers.stream()
                .map(relation -> {
                    User target = relation.getFollower();
                    boolean isFollowing = myFollowingIds.contains(target.getId());
                    return toDto(target, isFollowing);
                })
                .toList();
    }


    // 다른 유저 팔로잉 목록
    public List<SimpleUserProfileDto> getFollowingsByUserId(String myEmail, Long userId) {
        User me = getUserByEmail(myEmail);
        User targetUser = getUserById(userId);

        List<Long> myFollowingIds = followRepository.findFollowingIdsByFollower(me);
        List<Follow> followings = followRepository.findByFollower(targetUser);

        return followings.stream()
                .map(relation -> {
                    User target = relation.getFollowing();
                    boolean isFollowing = myFollowingIds.contains(target.getId());
                    return toDto(target, isFollowing);
                })
                .toList();
    }

    // 다른 유저 팔로워 목록
    public List<SimpleUserProfileDto> getFollowersByUserId(String myEmail, Long userId) {
        User me = getUserByEmail(myEmail);
        User targetUser = getUserById(userId);

        List<Long> myFollowingIds = followRepository.findFollowingIdsByFollower(me);
        List<Follow> followers = followRepository.findByFollowing(targetUser);

        return followers.stream()
                .map(relation -> {
                    User target = relation.getFollower();
                    boolean isFollowing = myFollowingIds.contains(target.getId());
                    return toDto(target, isFollowing);
                })
                .toList();
    }


    // 팔로우 / 언팔로우
    @Transactional
    public void follow(String email, Long targetId) {
        User me = getUserByEmail(email);
        User target = getUserById(targetId);

        if (me.equals(target)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }

        if (followRepository.existsByFollowerAndFollowing(me, target)) {
            return;
        }

        followRepository.save(new Follow(me, target));

        notificationService.notifyFollow(
                target.getId(),
                me.getId(),
                me.getNickname()
        );
    }

    @Transactional
    public void unfollow(String email, Long targetId) {
        User me = getUserByEmail(email);
        User target = getUserById(targetId);

        followRepository.deleteByFollowerAndFollowing(me, target);
    }

    // 프로필 요약
    public SimpleUserProfileDto getUserProfile(String myEmail, Long targetUserId) {
        User me = getUserByEmail(myEmail);
        User target = getUserById(targetUserId);

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(me, target);

        return toDto(target, isFollowing);
    }

    // 공통 유틸
    private SimpleUserProfileDto toDto(User target, boolean isFollowing) {
        int postCount = exhibitionRecordRepository.countByUser(target);
        int followerCount = followRepository.countByFollowing(target);
        int followingCount = followRepository.countByFollower(target);

        return SimpleUserProfileDto.from(
                target,
                isFollowing,
                postCount,
                followerCount,
                followingCount
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 없음"));
    }
}
