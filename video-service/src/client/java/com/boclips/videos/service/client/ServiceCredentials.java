package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServiceCredentials {
    private String accessTokenUri;
    private String clientId;
    private String clientSecret;
}
