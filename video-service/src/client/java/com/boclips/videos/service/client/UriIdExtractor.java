package com.boclips.videos.service.client;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriIdExtractor {

    private static Pattern uriPattern = Pattern.compile(".*videos/([0-9a-f]{24}).*");

    public static String extractId(URI uri) {
        String path = uri.getPath();
        Matcher matcher = uriPattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Not a video uri: " + uri);
    }
}
