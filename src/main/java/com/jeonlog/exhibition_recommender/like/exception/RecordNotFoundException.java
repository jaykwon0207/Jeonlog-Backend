package com.jeonlog.exhibition_recommender.like.exception;

// 전시기록이 존재하지 않을 때 던지는 예외
public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException(Long recordId) {
        super("전시기록(ID=" + recordId + ")이 존재하지 않습니다.");
    }
}