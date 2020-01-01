package com.boclips.videos.service.client.internal.resources;

import lombok.Data;

import java.util.List;

@Data
public class CollectionsResource {
    private EmbeddedCollectionsResource _embedded;

    public List<CollectionResource> getCollections() {
        return this._embedded.getCollections();
    }
}

@Data
class EmbeddedCollectionsResource {
    private List<CollectionResource> collections;
}
