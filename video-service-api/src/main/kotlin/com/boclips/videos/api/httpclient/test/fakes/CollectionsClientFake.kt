package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.api.response.collection.CollectionsResource
import com.boclips.videos.api.response.collection.CollectionsWrapperResource

class CollectionsClientFake : CollectionsClient, FakeClient<CollectionResource> {
    private val database: MutableMap<String, CollectionResource> = LinkedHashMap()

    override fun getCollections(collectionFilterRequest: CollectionFilterRequest): CollectionsResource {
        return CollectionsResource(_embedded = CollectionsWrapperResource(collections = database.values.toList()))
    }

    override fun getCollection(collectionId: String, projection: Projection?): CollectionResource {
        return database[collectionId] ?: throw FakeClient.notFoundException("Collection not found")
    }

    override fun add(element: CollectionResource): CollectionResource {
        return element.apply {
            database[element.id!!] = element
        }
    }

    override fun clear() {
        database.clear()
    }

    override fun findAll(): List<CollectionResource> {
        return database.values.toList()
    }
}
