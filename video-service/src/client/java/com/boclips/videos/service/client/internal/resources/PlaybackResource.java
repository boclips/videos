package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Playback;
import lombok.Data;

import java.time.Duration;

@Data
public class PlaybackResource {
    private String id;
    private String thumbnailUrl;
    private Duration duration;

    public Playback toPlayback() {
        return Playback.builder()
                .playbackId(id)
                .duration(duration)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
