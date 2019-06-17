package com.boclips.videos.service.client.internal.resources;

import lombok.*;

import java.net.URI;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Link {
    private String href;

    @SneakyThrows
    public URI toUri() {
        return new URI(this.href);
    }

    public Link interpolate(Map<String, Object> params) {
        var newHref = href;

        for (Map.Entry<String, Object> keyValue : params.entrySet()) {
            val valueToReplace = String.format("{%s}", keyValue.getKey());
            val valueToInsert = keyValue.getValue();
            newHref = newHref.replace(valueToReplace, valueToInsert.toString());
        }

        val newLink = new Link();
        newLink.href = newHref;
        return newLink;
    }
}
