package com.jeonlog.exhibition_recommender.common.file;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Profile("prod")   // 🔥 local에서는 FileController 자체가 등록되지 않음
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;
    private final CloudFrontUrlResolver cloudFrontUrlResolver;


    @GetMapping("/presign")
    public ApiResponse<String> getPresignedUrl(@CurrentUser User user, @RequestParam String filename) {
        String url = s3Service.generateUploadUrl(user.getId(), filename);
        log.info("✅ Presigned URL 생성 완료: {}", url);
        return ApiResponse.ok(url);
    }

    @GetMapping("/profile/presign")
    public ApiResponse<String> getProfilePresignedUrl(@CurrentUser User user,
                                                      @RequestParam String filename) {
        String url = s3Service.generateProfileUploadUrl(user.getId(), filename);
        log.info("✅ 프로필 Presigned URL 생성 완료: {}", url);
        return ApiResponse.ok(url);
    }

    @PostMapping("/confirm")
    public ApiResponse<String> confirmUpload(@CurrentUser User user,
                                             @RequestBody Map<String, String> body) {

        String fileKey = body.get("fileKey");

        // CloudFront URL ⭕
        String cloudFrontUrl = cloudFrontUrlResolver.resolve(fileKey);

        log.info("✅ 업로드 완료: userId={}, fileKey={}, cloudFrontUrl={}",
                user.getId(), fileKey, cloudFrontUrl);

        return ApiResponse.ok(cloudFrontUrl);
    }


}