package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Subject;
import com.boclips.videos.service.client.SubjectId;
import com.boclips.videos.service.client.Video;
import com.boclips.videos.service.client.VideoId;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class VideoResource {
    private VideoLinks _links = null;
    private String title = null;
    private String description = null;
    private LocalDate releasedOn = null;
    private Set<SubjectResource> subjects = Collections.emptySet();
    private String createdBy = null;
    private String contentPartner = null;
    private String contentPartnerVideoId = null;
    private PlaybackResource playback = null;

    public Video toVideo() {
        return Video.builder()
                .videoId(new VideoId(_links.getSelf().toUri()))
                .title(title)
                .description(description)
                .releasedOn(releasedOn)
                .createdBy(createdBy)
                .contentPartnerId(contentPartner)
                .contentPartnerVideoId(contentPartnerVideoId)
                .playback(playback != null ? playback.toPlayback() : null)
                .subjects(subjects.stream().map(subjectResource -> {
                    SubjectId subjectId = SubjectId.builder().value(subjectResource.getId()).build();

                    return Subject.builder()
                            .id(subjectId)
                            .name(subjectResource.getName())
                            .build();
                }).collect(Collectors.toSet()))
                .build();
    }

}
