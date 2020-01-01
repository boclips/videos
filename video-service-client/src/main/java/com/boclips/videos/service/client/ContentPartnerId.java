package com.boclips.videos.service.client;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
@AllArgsConstructor
public class ContentPartnerId {
    @NonNull
    private final String value;
}
