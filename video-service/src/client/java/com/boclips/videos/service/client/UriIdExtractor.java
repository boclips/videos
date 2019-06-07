package com.boclips.videos.service.client;

import java.net.URI;
import java.util.Arrays;

public class UriIdExtractor {

    public static String extractId(URI uri) {
        return Arrays.stream(uri.getPath().split("/"))
                .filter(s -> !s.isEmpty())
                .reduce((s1, s2) -> s2)
                .get();

    }
}
