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
    @NonNull
    private final List<VideoId> videos;
    @NonNull
    private final Set<SubjectId> subjects;
}


