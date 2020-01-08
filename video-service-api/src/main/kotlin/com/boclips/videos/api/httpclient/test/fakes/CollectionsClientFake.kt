package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.api.response.collection.CollectionsResource
import org.springframework.hateoas.Resource

class CollectionsClientFake : CollectionsClient {
    override fun getCollections(collectionFilterRequest: CollectionFilterRequest): CollectionsResource {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCollection(collectionId: String): CollectionResource {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
