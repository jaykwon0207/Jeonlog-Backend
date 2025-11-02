package com.jeonlog.exhibition_recommender.common.file;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;

    @GetMapping("/presign")
    public ApiResponse<String> getPresignedUrl(
            @CurrentUser User user,
            @RequestParam String filename
    ) {
        try {
            String url = s3Service.generateUploadUrl(user.getId(), filename);
            log.info("✅ Presigned URL 생성 완료: {}", url);
            return ApiResponse.ok(url);
        } catch (Exception e) {
            log.error("❌ Presigned URL 생성 실패: {}", e.getMessage());
            return ApiResponse.error("S3_GENERATE_FAIL", "Presigned URL 생성 중 오류가 발생했습니다.");
        }
    }
}