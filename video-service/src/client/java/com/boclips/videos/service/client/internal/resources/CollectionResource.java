package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.*;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CollectionResource {
    private CollectionLinks _links;
    private String title;
    private Set<SubjectResource> subjects;
    private List<VideoResource> videos;

    public Collection toCollection() {
        Set<Subject> subjects = this.subjects.stream()
                .map(subjectResource -> Subject.builder()
                        .name(subjectResource.getName())
                        .id(SubjectId.builder().value(subjectResource.getId()).build())
                        .build())
                .collect(Collectors.toSet());

        List<Video> videos = this.videos.stream()
                .map(VideoResource::toVideo)
                .collect(Collectors.toList());

        return Collection.builder()
                .collectionId(new CollectionId(_links.getSelf().toUri()))
                .title(title)
                .videos(videos)
                .subjects(subjects)
                .build();
    }
}
