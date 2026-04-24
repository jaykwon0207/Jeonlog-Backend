package com.jeonlog.exhibition_recommender.common.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Profile({"dev", "prod"})
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String generateUploadUrl(Long userId, String filename) {
        String lower = filename.toLowerCase();
        String folder = (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi"))
                ? "videos"
                : "images";

        String key = String.format("records/%s/%d_%s_%s",
                folder, userId, UUID.randomUUID(), filename);

        return createPresignedUrl(key);
    }

    public String generateProfileUploadUrl(Long userId, String filename) {
        String key = String.format("profile/%d_%s_%s", userId, UUID.randomUUID(), filename);
        return createPresignedUrl(key);
    }

    private String createPresignedUrl(String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        return presigned.url().toString();
    }


}