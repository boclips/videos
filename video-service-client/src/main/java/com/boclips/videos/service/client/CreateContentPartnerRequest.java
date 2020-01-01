package com.boclips.videos.service.client;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class CreateContentPartnerRequest {
    @NonNull
    private final String name;
    private final String accreditedToYtChannelId;
}
