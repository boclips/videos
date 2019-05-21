package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Video;
import com.boclips.videos.service.client.VideoId;
import lombok.Data;

import java.util.Set;

@Data
public class VideoResource {
    private VideoLinks _links = null;
    private String title = null;
    private String description = null;
    private Set<String> subjects = null;
    private String contentPartner = null;
    private String contentPartnerVideoId = null;

    public Video toVideo() {
        return Video.builder()
                .videoId(new VideoId(_links.getSelf().toUri()))
                .title(title)
                .description(description)
                .subjects(subjects)
                .contentPartnerId(contentPartner)
                .contentPartnerVideoId("")
                .build();
    }

}
