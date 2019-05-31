package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class Subject {
    private final SubjectId id;
    private final String name;
}
