package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Builder(toBuilder = true)
@Data
public class Video {
    private final VideoId videoId;
    private final String title;
    private final String description;
    private final LocalDate releasedOn;
    private final String createdBy;
    private final String contentPartnerId;
    private final String contentPartnerVideoId;
    private final Playback playback;
    private final Set<Subject> subjects;
    private final VideoType type;
}
