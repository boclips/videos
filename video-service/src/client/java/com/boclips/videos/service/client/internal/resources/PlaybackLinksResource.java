package com.boclips.videos.service.client.internal.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlaybackLinksResource {
    private Link thumbnail;
    private Link videoPreview;
    private Link createPlaybackEvent;
    private Link createPlayerInteractedWithEvent;
}
