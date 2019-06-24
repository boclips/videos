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
        Set<SubjectId> subjects = this.subjects.stream()
                .map(subjectResource -> new SubjectId(subjectResource.getId()))
                .collect(Collectors.toSet());

        List<Video> videos = this.videos.stream()
                .map(videoResource -> Video.builder()
                        .videoId(new VideoId(videoResource.get_links().getSelf().toUri()))
                        .title(videoResource.getTitle())
                        .description(videoResource.getDescription())
                        .contentPartnerId(videoResource.getContentPartner())
                        .contentPartnerVideoId(videoResource.getContentPartnerVideoId())
                        .playback(videoResource.getPlayback() != null ? videoResource.getPlayback().toPlayback() : null)
                        .build()
                )
                .collect(Collectors.toList());

        return Collection.builder()
                .collectionId(new CollectionId(_links.getSelf().toUri()))
                .title(title)
                .videos(videos)
                .subjects(subjects)
                .build();
    }
}
