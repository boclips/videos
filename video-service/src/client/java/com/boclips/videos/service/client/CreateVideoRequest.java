package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Builder
@Data
public class CreateVideoRequest {
    private final @NonNull String provider;
    private final @NonNull String providerVideoId;
    private final @NonNull String title;
    private final @NonNull String description;
    private final @NonNull LocalDate releasedOn;
    private final String legalRestrictions;
    private final @NonNull List<String> keywords;
    private final @NonNull VideoType videoType;
    private final @NonNull String playbackId;
    private final @NonNull PlaybackProvider playbackProvider;
    private final @NonNull Set<String> subjects;
}
