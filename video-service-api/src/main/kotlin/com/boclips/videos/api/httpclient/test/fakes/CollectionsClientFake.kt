package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.api.response.collection.CollectionsResource
import com.boclips.videos.api.response.collection.CollectionsWrapperResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.VideoResource
import kotlin.random.Random

class CollectionsClientFake : CollectionsClient, FakeClient<CollectionResource> {
    private val database: MutableMap<String, CollectionResource> = LinkedHashMap()

    override fun create(createCollectionRequest: CreateCollectionRequest): CollectionResource {
        val randomGenerator = Random(10)
        val resourceId = randomGenerator.nextInt().toString()

        val resource = CollectionResource(
            id = resourceId,
            owner = "some-owner",
            title = createCollectionRequest.title,
            description = createCollectionRequest.description,
            videos = createCollectionRequest.videos.map {
                VideoResource(
                    id = randomGenerator.nextInt().toString(),
                    _links = null
                )
            },
            discoverable = createCollectionRequest.discoverable,
            subjects = createCollectionRequest.subjects.map {
                SubjectResource(
                    id = randomGenerator.nextInt().toString()
                )
            }
                .toSet(),
            ageRange = null,
            attachments = setOf(),
            units = listOf(),
            _links = null
        )

        database[resourceId] = resource
        return resource
    }

    override fun update(collectionId: String, update: UpdateCollectionRequest): Void {
        TODO("not implemented")
    }

    override fun delete(collectionId: String) {
        database.remove(collectionId)
    }

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
