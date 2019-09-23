package com.boclips.videos.service.client.internal.resources;

import lombok.Data;

@Data
public class PlaybackLinks {
    private Link thumbnail;
    private Link videoPreview;
    private Link createPlaybackEvent;
    private Link createPlayerInteractedWithEvent;
}
