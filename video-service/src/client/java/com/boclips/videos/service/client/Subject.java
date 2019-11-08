package com.boclips.videos.service.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
public class Subject {
    private final SubjectId id;
    private final String name;
}
