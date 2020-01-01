package com.boclips.videos.service.client.internal.resources;

import com.boclips.videos.service.client.ContentPartner;
import com.boclips.videos.service.client.ContentPartnerId;
import lombok.Data;

import java.util.Currency;

@Data
public class ContentPartnerResource {
    private String id = null;
    private String name = null;
    private Boolean official = null;
    private String currency = null;

    public ContentPartner toContentPartner() {
        return ContentPartner.builder()
                .name(this.name)
                .contentPartnerId(
                        ContentPartnerId
                                .builder()
                                .value(this.id)
                                .build()
                )
                .official(this.official)
                .currency(this.currency == null ? null : Currency.getInstance(this.currency))
                .build();
    }
}