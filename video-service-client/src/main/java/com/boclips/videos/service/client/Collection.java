package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

@Builder(toBuilder = true)
@Data
public class Collection {
    @NonNull
    private final CollectionId collectionId;
    @NonNull
    private final String title;
    private final String description;
    @NonNull
    private final List<Video> videos;
    @NonNull
    private final Set<Subject> subjects;
    private final Boolean isPublic;
    private final Boolean mine;
}


