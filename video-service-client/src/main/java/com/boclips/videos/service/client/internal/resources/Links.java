package com.boclips.videos.service.client.internal.resources;

import lombok.Data;

@Data
public class Links {
    private Link video;
    private Link subjects;
    private Link collection;
    private Link createCollection;
    private Link searchPublicCollections;
    private Link searchCollections;
    private Link myCollections;
    private Link collectionsByOwner;
    private Link contentPartners;
    private Link contentPartner;
}