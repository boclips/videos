package com.boclips.videos.service.client;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriIdExtractor {

    public final static Pattern VIDEO_ID_URI_PATTERN = Pattern.compile(".*videos/([0-9a-f]{24}).*");
    public final static Pattern CONTENT_PARTNER_ID_URI_PATTERN = Pattern.compile(".*content-partners/([0-9a-f]{24}).*");

    public static String extractId(URI uri, Pattern uriPattern) {
        String path = uri.getPath();
        Matcher matcher = uriPattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Not a video uri: " + uri);
    }
}
