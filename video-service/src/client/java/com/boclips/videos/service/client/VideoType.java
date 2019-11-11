package com.boclips.videos.service.client;

import java.util.Arrays;

public enum VideoType {
    NEWS(1),
    STOCK(2),
    INSTRUCTIONAL_CLIPS(3);

    private final Integer id;

    VideoType(Integer id) {
        this.id = id;
    }

    public static VideoType fromId(Integer id) {
        return Arrays.stream(VideoType.values()).filter(videoType -> videoType.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Video type id not recognised: " + id));
    }
}
