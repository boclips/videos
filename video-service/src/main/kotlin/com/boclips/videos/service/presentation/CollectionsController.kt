package com.boclips.videos.service.presentation

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.collection.*
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import getCurrentUserId
import mu.KLogging
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/collections")
class CollectionsController(
    private val createCollection: CreateCollection,
    private val getUserCollections: GetUserCollections,
    private val getCollection: GetCollection,
    private val addVideoToCollection: AddVideoToCollection,
    private val removeVideoFromCollection: RemoveVideoFromCollection,
    private val updateCollection: UpdateCollection,
    private val deleteCollection: DeleteCollection
) {
    enum class Projections {
        details,
        list
    }

    companion object : KLogging() {
        fun getUserCollectionsDetailsLink() = linkTo(
            methodOn(CollectionsController::class.java).getAllUserCollections(
                Projections.details
            )
        )

        fun getUserCollectionsListLink() = linkTo(
            methodOn(CollectionsController::class.java).getAllUserCollections(
                Projections.list
            )
        )

        fun getUserCollectionLink(id: String?) = linkTo(
            methodOn(CollectionsController::class.java).show(
                id
            )
        )

        fun postUserCollectionsLink() = linkTo(methodOn(CollectionsController::class.java).postCollection(null))
    }

    @PostMapping
    fun postCollection(@RequestBody createCollectionRequest: CreateCollectionRequest?): ResponseEntity<Void> {
        val collection = createCollection(createCollectionRequest)
        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, getUserCollectionLink(collection.id.value).toUri().toString())
        }
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    @PatchMapping("/{id}")
    fun patchCollection(@PathVariable("id") id: String, @RequestBody request: UpdateCollectionRequest?): ResponseEntity<Void> {
        updateCollection(id, request)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeCollection(@PathVariable("id") id: String) {
        deleteCollection(id)
    }

    @GetMapping
    fun getAllUserCollections(@RequestParam projection: Projections): Resources<Resource<CollectionResource>> {
        val selfLink = when (projection) {
            Projections.details -> getUserCollectionsDetailsLink().withSelfRel()
            Projections.list -> getUserCollectionsListLink().withSelfRel()
        }

        val detailsLink = getUserCollectionsDetailsLink().withRel("details")
        val listLink = getUserCollectionsListLink().withRel("list")

        return Resources(getUserCollections(projection).map(::wrapCollection), selfLink, detailsLink, listLink)
    }

    @GetMapping("/{id}")
    fun show(@PathVariable("id") id: String?): Resource<CollectionResource> {
        return wrapCollection(getCollection(id))
    }

    @PutMapping("/{collection_id}/videos/{video_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addVideo(@PathVariable("collection_id") collectionId: String?, @PathVariable("video_id") videoId: String?): Any? {
        addVideoToCollection(collectionId = collectionId, videoId = videoId)

        return null
    }

    @DeleteMapping("/{collection_id}/videos/{video_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeVideo(@PathVariable("collection_id") collectionId: String?, @PathVariable("video_id") videoId: String?): Any? {
        removeVideoFromCollection(collectionId, videoId)

        return null
    }

    private fun wrapCollection(collection: CollectionResource): Resource<CollectionResource> {
        val links = mutableListOf(getUserCollectionLink(collection.id).withSelfRel())

        if (collection.isOwnedByCurrentUser()) {
            links.add(linkTo(
                    methodOn(CollectionsController::class.java).patchCollection(id = collection.id, request = null)
            ).withRel("edit"))

            links.add(linkTo(
                    methodOn(CollectionsController::class.java).addVideo(collectionId = collection.id, videoId = null)
            ).withRel("addVideo"))

            links.add(linkTo(
                    methodOn(CollectionsController::class.java).removeVideo(collectionId = collection.id, videoId = null)
            ).withRel("removeVideo"))
        }

        return Resource(collection, links)
    }
}

private fun CollectionResource.isOwnedByCurrentUser() = this.owner == UserExtractor.getCurrentUserId().value