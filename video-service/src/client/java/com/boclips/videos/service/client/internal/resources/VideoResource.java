package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.VideoId;
import com.boclips.videos.service.client.Video;
import lombok.Data;

import java.util.Set;

@Data
public class VideoResource {
    private VideoLinks _links = null;
    private Set<String> subjects = null;
    private String contentPartner = null;
    private String contentPartnerVideoId = null;

    public Video toVideo() {
        return Video.builder()
                .videoId(new VideoId(_links.getSelf().toUri()))
                .subjects(subjects)
                .contentPartnerId(contentPartner)
                .contentPartnerVideoId("")
                .build();
    }

}
