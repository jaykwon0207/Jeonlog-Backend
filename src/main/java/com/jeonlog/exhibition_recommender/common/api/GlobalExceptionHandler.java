package com.jeonlog.exhibition_recommender.common.api;

import com.jeonlog.exhibition_recommender.exhibition.exception.VenueNotFoundException;
import com.jeonlog.exhibition_recommender.like.exception.RecordNotFoundException;
import com.jeonlog.exhibition_recommender.user.exception.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 장소 없음
    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleVenueNotFound(VenueNotFoundException e, HttpServletRequest request) {
        log.warn("[ERROR] venue_not_found endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("VENUE_NOT_FOUND", e.getMessage()));
    }

    // 전시 없음
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        log.warn("[ERROR] entity_not_found endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("ENTITY_NOT_FOUND", e.getMessage()));
    }

    // 사용자 없음
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
        log.warn("[ERROR] user_not_found endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("USER_NOT_FOUND", e.getMessage()));
    }

    // 잘못된 요청
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[ERROR] invalid_argument endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", e.getMessage()));
    }

    @ExceptionHandler({DateTimeParseException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(Exception e, HttpServletRequest request) {
        log.warn("[ERROR] invalid_argument_type endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", "요청 파라미터 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("[ERROR] forbidden endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", e.getMessage()));
    }

    // 기록 없음
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecordNotFound(RecordNotFoundException e, HttpServletRequest request) {
        log.warn("[ERROR] record_not_found endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RECORD_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(AwsServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleAwsException(AwsServiceException e, HttpServletRequest request) {
        log.error("[ERROR] aws_exception endpoint={} method={} reason={}",
                request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("AWS_ERROR", "S3 Presigned URL 생성 실패: " + e.getMessage()));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception e, HttpServletRequest request) {
        log.error(
                "[ERROR] unhandled_exception endpoint={} method={} exception={} reason={}",
                request.getRequestURI(),
                request.getMethod(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                e
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR",
                        e.getClass().getSimpleName() + ": " + e.getMessage()));
    }
}
