package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.VideoId;
import com.boclips.videos.service.client.spring.Video;
import lombok.Data;

import java.net.URI;
import java.util.Set;

@Data
public class VideoResource {
    private Links _links = null;
    private Set<String> subjects = null;
    private String contentPartner = null;
    private String contentPartnerVideoId = null;

    Video toVideo() {
        return Video.builder()
                .videoId(new VideoId(_links.self.href))
                .subjects(subjects)
                .contentPartnerId(contentPartner)
                .contentPartnerVideoId("")
                .build();
    }

    @Data
    public static class Link {
        private URI href = null;
    }

    @Data
    public static class Links {
        private Link self;
    }
}