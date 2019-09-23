package com.boclips.videos.service.client.internal.resources;


import com.boclips.videos.service.client.ContentPartner;
import com.boclips.videos.service.client.ContentPartnerId;
import lombok.Data;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ContentPartnersResource {
    private EmbeddedContentPartnersResource _embedded;

    public List<ContentPartner> toContentPartners() {
        if (this._embedded == null) {
            return new ArrayList<>();
        }

        return this._embedded.getContentPartners().stream()
                .map(ContentPartnerResource::toContentPartner)
                .collect(Collectors.toList());
    }
}

@Data
class EmbeddedContentPartnersResource {
    private List<ContentPartnerResource> contentPartners;
}

@Data
class ContentPartnerResource {
    private String id = null;
    private String name = null;
    private Boolean official = null;
    private String currency = null;

    ContentPartner toContentPartner() {
        return ContentPartner.builder()
                .name(this.name)
                .contentPartnerId(
                        ContentPartnerId
                                .builder()
                                .value(this.id)
                                .build()
                )
                .official(this.official)
                .currency(Currency.getInstance(this.currency))
                .build();
    }
}
