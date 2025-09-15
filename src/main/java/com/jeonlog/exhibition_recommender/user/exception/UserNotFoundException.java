package com.jeonlog.exhibition_recommender.user.exception;

// 사용자 조회 실패 시 던지는 예외
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}