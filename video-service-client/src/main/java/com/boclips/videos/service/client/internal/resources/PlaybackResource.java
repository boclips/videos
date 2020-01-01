package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Playback;
import com.boclips.videos.service.client.PlaybackLinks;
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
    private PlaybackLinksResource _links;
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
                .links(new PlaybackLinks(
                        _links.getCreatePlaybackEvent()
                ))
                .build();
    }
}
