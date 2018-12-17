package com.boclips.videos.service.client.internal.resources;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinksResource {
    private Links _links;
}
