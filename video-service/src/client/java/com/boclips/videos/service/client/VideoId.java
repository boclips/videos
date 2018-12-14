package com.boclips.videos.service.client;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;

@Data
@AllArgsConstructor
public class VideoId {
    private final URI uri;
}