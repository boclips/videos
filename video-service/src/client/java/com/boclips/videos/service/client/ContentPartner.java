package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class ContentPartner {
    private final ContentPartnerId contentPartnerId;
    private final String name;
    private final Boolean official;
}