package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Subject;
import com.boclips.videos.service.client.SubjectId;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SubjectsResource {
    private EmbeddedSubjectsResource _embedded;

    public List<Subject> toSubjects() {
        if (this._embedded == null) {
            return new ArrayList<>();
        }

        return this._embedded.getSubjects().stream().map(subjectResource ->
                Subject.builder()
                        .id(SubjectId.builder().value(subjectResource.getId()).build())
                        .name(subjectResource.getName())
                        .build()
        ).collect(Collectors.toList());
    }
}

@Data
class SubjectResource {
    private String id = null;
    private String name = null;
}

@Data
class EmbeddedSubjectsResource {
    private List<SubjectResource> subjects;
}
