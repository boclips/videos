package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Builder
@Data
public class Playback {
    private final String playbackId;
    private final String thumbnailUrl;
    private final Duration duration;
    private final String referenceId;
}
