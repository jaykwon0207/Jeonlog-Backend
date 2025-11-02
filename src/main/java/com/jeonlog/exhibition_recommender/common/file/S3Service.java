package com.jeonlog.exhibition_recommender.common.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String generateUploadUrl(Long userId, String filename) {
        LocalDate now = LocalDate.now();

        // 확장자 추출
        String lower = filename.toLowerCase();
        String folder = (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi"))
                ? "videos"
                : "images";

        // 📂 S3 저장 경로: records/{images|videos}/YYYY/MM/DD/{userId}_{UUID}_{filename}
        String key = String.format(
                "records/%s/%d/%02d/%02d/%d_%s_%s",
                folder,
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                userId,
                UUID.randomUUID(),
                filename
        );

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