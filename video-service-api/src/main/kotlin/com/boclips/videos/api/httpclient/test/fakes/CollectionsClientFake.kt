package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.response.collection.CollectionResource
import org.springframework.hateoas.Resource

class CollectionsClientFake : CollectionsClient {
    override fun collection(collectionId: String): Resource<CollectionResource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}