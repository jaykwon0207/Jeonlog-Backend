package com.jeonlog.exhibition_recommender.common.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CloudFrontUrlResolver {

    @Value("${cloudfront.domain}")
    private String cloudfrontDomain;

    public String resolve(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return "https://" + cloudfrontDomain + "/" + key;
    }
}