package com.boclips.videos.service.client.exceptions;

import com.boclips.videos.service.client.VideoId;

public class VideoNotFoundException extends RuntimeException {

    public VideoNotFoundException(VideoId videoId) {
        super(videoId.getUri().toString());
    }
}