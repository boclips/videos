package com.boclips.videos.service.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
@AllArgsConstructor
public class SubjectId {
    @NonNull
    private final String value;
}
