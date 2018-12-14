package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Builder
@Data
public class CreateVideoRequest {
    private final String provider;
    private final String providerVideoId;
    private final String title;
    private final String description;
    private final LocalDate releasedOn;
    private final Duration duration;
    private final String legalRestrictions;
    private final List<String> keywords;
    private final String contentType;
    private final String playbackId;
    private final PlaybackProvider playbackProvider;
    private final Set<String> subjects;
}
