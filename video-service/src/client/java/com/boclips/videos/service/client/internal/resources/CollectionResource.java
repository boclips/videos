package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Collection;
import com.boclips.videos.service.client.SubjectId;
import com.boclips.videos.service.client.VideoId;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CollectionResource {

    private Set<SubjectResource> subjects;

    private List<VideoResource> videos;

    public Collection toCollection() {
        Set<SubjectId> subjects = this.subjects.stream()
                .map(subjectResource -> new SubjectId(subjectResource.getId()))
                .collect(Collectors.toSet());

        List<VideoId> videos = this.videos.stream()
                .map(videoResource -> new VideoId(videoResource.get_links().getSelf().toUri()))
                .collect(Collectors.toList());

        return Collection.builder()
                .videos(videos)
                .subjects(subjects)
                .build();
    }
}
