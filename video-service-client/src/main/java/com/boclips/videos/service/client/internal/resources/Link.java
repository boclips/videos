package com.boclips.videos.service.client.internal.resources;

import com.damnhandy.uri.template.UriTemplate;
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
        val newLink = new Link();
        newLink.href = UriTemplate.fromTemplate(href).expand(params);
        return newLink;
    }
}
