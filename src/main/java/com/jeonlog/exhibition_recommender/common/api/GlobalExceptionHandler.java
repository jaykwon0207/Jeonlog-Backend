package com.jeonlog.exhibition_recommender.common.api;

import com.jeonlog.exhibition_recommender.exhibition.exception.VenueNotFoundException;
import com.jeonlog.exhibition_recommender.like.exception.RecordNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleVenueNotFound(VenueNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("VENUE_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecordNotFound(RecordNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RECORD_NOT_FOUND", e.getMessage()));
    }


}
