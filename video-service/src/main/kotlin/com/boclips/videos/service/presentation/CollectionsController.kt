package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetPublicCollections
import com.boclips.videos.service.application.collection.GetUserCollections
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UpdateCollection
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/collections")
class CollectionsController(
    private val createCollection: CreateCollection,
    private val getUserCollections: GetUserCollections,
    private val getCollection: GetCollection,
    private val addVideoToCollection: AddVideoToCollection,
    private val removeVideoFromCollection: RemoveVideoFromCollection,
    private val updateCollection: UpdateCollection,
    private val deleteCollection: DeleteCollection,
    private val getPublicCollections: GetPublicCollections
) {
    enum class Projections {
        details,
        list
    }

    companion object : KLogging() {
        fun getUserCollectionsDetailsLink(owner: String?) = linkTo(
            methodOn(CollectionsController::class.java).getCollections(
                Projections.details,
                owner
            )
        )

        fun getUserCollectionsListLink(owner: String?) = linkTo(
            methodOn(CollectionsController::class.java).getCollections(
                Projections.list,
                owner
            )
        )

        fun getUserCollectionLink(id: String?) = linkTo(
            methodOn(CollectionsController::class.java).show(
                id
            )
        )

        fun postUserCollectionsLink() = linkTo(methodOn(CollectionsController::class.java).postCollection(null))

        fun getPublicCollections() = linkTo(
            methodOn(CollectionsController::class.java).getCollections(
                Projections.list,
                null
            )
        )
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
    fun removeCollection(@PathVariable("id") id: String): ResponseEntity<Void> {
        deleteCollection(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @GetMapping
    fun getCollections(@RequestParam projection: Projections, @RequestParam owner: String?): Resources<Resource<CollectionResource>> {
        val selfLink = when (projection) {
            Projections.details -> getUserCollectionsDetailsLink(owner).withSelfRel()
            Projections.list -> getUserCollectionsListLink(owner).withSelfRel()
        }

        val detailsLink = getUserCollectionsDetailsLink(owner).withRel("details")
        val listLink = getUserCollectionsListLink(owner).withRel("list")

        val collections = if (owner != null ){
            getUserCollections(projection)
        } else {
            getPublicCollections(projection)
        }

        return Resources(collections.map(::wrapCollection), selfLink, detailsLink, listLink)
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
    fun removeVideo(@PathVariable("collection_id") collectionId: String?, @PathVariable("video_id") videoId: String?): ResponseEntity<Void> {
        removeVideoFromCollection(collectionId, videoId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    private fun wrapCollection(collection: CollectionResource): Resource<CollectionResource> {
        val links = mutableListOf(getUserCollectionLink(collection.id).withSelfRel())

        if (collection.isOwnedByCurrentUser()) {
            links.add(linkTo(
                    methodOn(CollectionsController::class.java).patchCollection(id = collection.id, request = null)
            ).withRel("edit"))
            links.add(linkTo(
                    methodOn(CollectionsController::class.java).removeCollection(id = collection.id)
            ).withRel("remove"))

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

private fun CollectionResource.isOwnedByCurrentUser() = this.owner == getCurrentUserId().value