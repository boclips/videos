package com.boclips.videos.service.client;

import lombok.Builder;
import lombok.Data;

import java.util.Currency;

@Builder(toBuilder = true)
@Data
public class ContentPartner {
    private final ContentPartnerId contentPartnerId;
    private final String name;
    private final Boolean official;
    private final Currency currency;
}