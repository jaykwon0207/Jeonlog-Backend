package com.jeonlog.exhibition_recommender.common.file;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;

    @GetMapping("/presign")
    public ApiResponse<String> getPresignedUrl(@CurrentUser User user, @RequestParam String filename) {
        String url = s3Service.generateUploadUrl(user.getId(), filename);
        log.info("✅ Presigned URL 생성 완료: {}", url);
        return ApiResponse.ok(url);
    }

    @PostMapping("/confirm")
    public ApiResponse<String> confirmUpload(@CurrentUser User user, @RequestBody Map<String, String> body) {
        String fileKey = body.get("fileKey");
        String fixedUrl = s3Service.getFileUrl(fileKey);
        log.info("✅ 파일 업로드 완료: userId={}, fileKey={}, fixedUrl={}", user.getId(), fileKey, fixedUrl);
        return ApiResponse.ok(fixedUrl);
    }
}