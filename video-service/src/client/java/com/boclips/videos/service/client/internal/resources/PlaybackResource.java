package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Playback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlaybackResource {
    private String id;
    private String thumbnailUrl;
    private Duration duration;
    private String referenceId;

    public Playback toPlayback() {
        return Playback.builder()
                .playbackId(id)
                .duration(duration)
                .thumbnailUrl(thumbnailUrl)
                .referenceId(referenceId)
                .build();
    }
}
