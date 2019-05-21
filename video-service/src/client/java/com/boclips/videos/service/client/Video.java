package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder(toBuilder = true)
@Data
public class Video {
    private final VideoId videoId;
    private final String title;
    private final String description;
    private final Set<String> subjects;
    private final String contentPartnerId;
    private final String contentPartnerVideoId;
}


