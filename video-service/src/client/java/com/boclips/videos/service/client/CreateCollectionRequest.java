package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Builder
@Data
public class CreateCollectionRequest {
    private final @NonNull String title;
    private final String description;
    private final List<String> videos;
    private final boolean isPublic;
}
