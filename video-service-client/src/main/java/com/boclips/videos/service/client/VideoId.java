package com.boclips.videos.service.client;

import lombok.Data;

import java.net.URI;

@Data
public class VideoId {
    private final String value;
    private final URI uri;

    public VideoId(URI uri) {
        this.value = UriIdExtractor.extractId(uri, UriIdExtractor.VIDEO_ID_URI_PATTERN);
        this.uri = uri;
    }
}
