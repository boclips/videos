package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.Subject;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class SubjectsResource {
    private EmbeddedSubjectsResource _embedded;

    public List<Subject> toSubjects() {
        return this._embedded.getSubjects().stream().map(subjectResource ->
                Subject.builder().id(subjectResource.getId()).name(subjectResource.getName()).build()
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