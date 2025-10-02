package com.jeonlog.exhibition_recommender.common.file;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {
    private final S3Service s3Service;

    @GetMapping("/presign")
    public ApiResponse<String> getPresignedUrl(@RequestParam String filename) {
        String key = "records/images/" + UUID.randomUUID() + "_" + filename;
        String url = s3Service.generateUploadUrl(key);
        return ApiResponse.ok(url);
    }
}