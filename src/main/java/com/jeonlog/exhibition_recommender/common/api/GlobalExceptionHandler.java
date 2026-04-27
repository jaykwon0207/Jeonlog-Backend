package com.jeonlog.exhibition_recommender.common.api;

import com.jeonlog.exhibition_recommender.common.logging.TraceIdFilter;
import com.jeonlog.exhibition_recommender.exhibition.exception.VenueNotFoundException;
import com.jeonlog.exhibition_recommender.like.exception.RecordNotFoundException;
import com.jeonlog.exhibition_recommender.user.exception.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

    private static final String VENUE_NOT_FOUND_CODE = "VENUE_NOT_FOUND";
    private static final String ENTITY_NOT_FOUND_CODE = "ENTITY_NOT_FOUND";
    private static final String USER_NOT_FOUND_CODE = "USER_NOT_FOUND";
    private static final String INVALID_ARGUMENT_CODE = "INVALID_ARGUMENT";
    private static final String FORBIDDEN_CODE = "FORBIDDEN";
    private static final String RECORD_NOT_FOUND_CODE = "RECORD_NOT_FOUND";
    private static final String AWS_ERROR_CODE = "AWS_ERROR";
    private static final String INTERNAL_ERROR_CODE = "INTERNAL_ERROR";

    private static final String VENUE_NOT_FOUND_MESSAGE = "요청한 장소를 찾을 수 없습니다.";
    private static final String ENTITY_NOT_FOUND_MESSAGE = "요청한 대상을 찾을 수 없습니다.";
    private static final String USER_NOT_FOUND_MESSAGE = "사용자를 찾을 수 없습니다.";
    private static final String INVALID_ARGUMENT_MESSAGE = "요청 값이 올바르지 않습니다.";
    private static final String FORBIDDEN_MESSAGE = "접근 권한이 없습니다.";
    private static final String RECORD_NOT_FOUND_MESSAGE = "전시기록을 찾을 수 없습니다.";
    private static final String AWS_ERROR_MESSAGE = "파일 처리 중 오류가 발생했습니다.";
    private static final String INTERNAL_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";

    // 장소 없음
    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleVenueNotFound(VenueNotFoundException e, HttpServletRequest request) {
        logClientError(VENUE_NOT_FOUND_CODE, request, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(VENUE_NOT_FOUND_CODE, VENUE_NOT_FOUND_MESSAGE));
    }

    // 전시 없음
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        logClientError(ENTITY_NOT_FOUND_CODE, request, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ENTITY_NOT_FOUND_CODE, ENTITY_NOT_FOUND_MESSAGE));
    }

    // 사용자 없음
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
        logClientError(USER_NOT_FOUND_CODE, request, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(USER_NOT_FOUND_CODE, USER_NOT_FOUND_MESSAGE));
    }

    // 잘못된 요청
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        logClientError(INVALID_ARGUMENT_CODE, request, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(INVALID_ARGUMENT_CODE, INVALID_ARGUMENT_MESSAGE));
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
        logClientError(FORBIDDEN_CODE, request, e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(FORBIDDEN_CODE, FORBIDDEN_MESSAGE));
    }

    // 기록 없음
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecordNotFound(RecordNotFoundException e, HttpServletRequest request) {
        logClientError(RECORD_NOT_FOUND_CODE, request, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(RECORD_NOT_FOUND_CODE, RECORD_NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(AwsServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleAwsException(AwsServiceException e, HttpServletRequest request) {
        logServerError(AWS_ERROR_CODE, request, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(AWS_ERROR_CODE, AWS_ERROR_MESSAGE));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception e, HttpServletRequest request) {
        logServerError(INTERNAL_ERROR_CODE, request, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(INTERNAL_ERROR_CODE, INTERNAL_ERROR_MESSAGE));
    }

    private void logClientError(String code, HttpServletRequest request, Exception e) {
        log.warn(
                "[ERROR] code={} traceId={} endpoint={} method={} reason={}",
                code,
                traceId(),
                request.getRequestURI(),
                request.getMethod(),
                e.getMessage()
        );
    }

    private void logServerError(String code, HttpServletRequest request, Exception e) {
        log.error(
                "[ERROR] code={} traceId={} endpoint={} method={} reason={}",
                code,
                traceId(),
                request.getRequestURI(),
                request.getMethod(),
                e.getMessage(),
                e
        );
    }

    private String traceId() {
        return MDC.get(TraceIdFilter.TRACE_ID_KEY);
    }
}
