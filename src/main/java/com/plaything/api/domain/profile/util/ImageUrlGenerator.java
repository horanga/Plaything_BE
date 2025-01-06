package com.plaything.api.domain.profile.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlGenerator {

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    public String getImageUrl(String imageKey) {
        return "https://" + cloudfrontDomain + "/" + imageKey;
    }

}
