package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetDefaultCollection
import com.boclips.videos.service.application.collection.GetUserCollections
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import mu.KLogging
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/collections")
class CollectionsController(
    private val createCollection: CreateCollection,
    private val getDefaultCollection: GetDefaultCollection,
    private val getUserCollections: GetUserCollections,
    private val getCollection: GetCollection,
    private val addVideoToCollection: AddVideoToCollection,
    private val removeVideoFromCollection: RemoveVideoFromCollection
) {
    companion object : KLogging() {
        fun getUserCollectionsLink() = linkTo(methodOn(CollectionsController::class.java).getAllUserCollections())
        fun getUserCollectionLink(id: String?) = linkTo(methodOn(CollectionsController::class.java).show(
            id))

        const val LEGACY_DEFAULT_COLLECTION = "default"
    }

    @PostMapping
    fun postCollection(@RequestBody createCollectionRequest: CreateCollectionRequest): ResponseEntity<Void> {
        val collection = createCollection(createCollectionRequest)
        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, getUserCollectionLink(collection.id.value).toUri().toString())
        }
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllUserCollections(): Resources<Resource<CollectionResource>> {
        val selfLink = getUserCollectionsLink().withSelfRel()
        return Resources(getUserCollections().map(::wrapCollection), selfLink)
    }

    @GetMapping("/{id}")
    fun show(@PathVariable("id") id: String?): Resource<CollectionResource> {
        return if (id == LEGACY_DEFAULT_COLLECTION) {
            wrapCollection(getDefaultCollection())
        } else {
            wrapCollection(getCollection(id))
        }
    }

    @PutMapping("/{collection_id}/videos/{video_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addVideo(@PathVariable("collection_id") collectionId: String?, @PathVariable("video_id") videoId: String?): Any? {
        if (collectionId == LEGACY_DEFAULT_COLLECTION) {
            addVideoToCollection(getDefaultCollection().id, videoId)
        } else {
            addVideoToCollection(collectionId = collectionId, videoId = videoId)
        }

        return null
    }

    @DeleteMapping("/{collection_id}/videos/{video_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeVideo(@PathVariable("collection_id") collectionId: String?, @PathVariable("video_id") videoId: String?): Any? {
        if (collectionId == LEGACY_DEFAULT_COLLECTION) {
            removeVideoFromCollection(getDefaultCollection().id, videoId)
        } else {
            removeVideoFromCollection(collectionId, videoId)
        }
        return null
    }

    private fun wrapCollection(collection: CollectionResource): Resource<CollectionResource> {
        val selfLink = getUserCollectionLink(collection.id).withSelfRel()
        val addVideoLink = linkTo(
            methodOn(CollectionsController::class.java)
                .addVideo(collectionId = collection.id, videoId = null)
        )
            .withRel("addVideo")
        val removeVideoLink = linkTo(
            methodOn(CollectionsController::class.java)
                .removeVideo(collectionId = collection.id, videoId = null)
        )
            .withRel("removeVideo")
        return Resource(collection, selfLink, addVideoLink, removeVideoLink)
    }
}