package com.boclips.videos.service.client.spring;

import com.boclips.videos.service.client.VideoId;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder(toBuilder = true)
@Data
public class Video {

    private final VideoId videoId;

    private final Set<String> subjects;

    private final String contentPartnerId;

    private final String contentPartnerVideoId;
}


