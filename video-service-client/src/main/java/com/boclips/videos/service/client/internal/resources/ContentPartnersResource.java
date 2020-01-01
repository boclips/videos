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
