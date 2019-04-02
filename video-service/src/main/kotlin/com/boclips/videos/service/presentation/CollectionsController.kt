package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
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
    private val getCollection: GetCollection,
    private val addVideoToCollection: AddVideoToCollection,
    private val removeVideoFromCollection: RemoveVideoFromCollection,
    private val updateCollection: UpdateCollection,
    private val deleteCollection: DeleteCollection,
    private val getCollections: GetCollections,
    private val collectionsLinkBuilder: CollectionsLinkBuilder
) {
    companion object : KLogging() {
        const val PUBLIC_COLLECTIONS_PAGE_SIZE = 30
    }

    @PostMapping
    fun postCollection(@RequestBody createCollectionRequest: CreateCollectionRequest?): ResponseEntity<Void> {
        val collection = createCollection(createCollectionRequest)
        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, collectionsLinkBuilder.collection(collection.id.value).href)
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
    fun getFilteredCollections(
        @RequestParam projection: Projections,
        @RequestParam public: Boolean = false,
        @RequestParam(required = false) owner: String?,
        @RequestParam page: Int?,
        @RequestParam size: Int?
    ): Resources<Resource<CollectionResource>> {
        val pageNum = page?.let { it } ?: 0
        val pageSize = size?.let { it } ?: PUBLIC_COLLECTIONS_PAGE_SIZE

        val collections = getCollections(projection, public, owner, pageNum, pageSize)

        return Resources(
            collections.elements.map(::wrapCollection),
            listOfNotNull(
                collectionsLinkBuilder.projections().list(),
                collectionsLinkBuilder.projections().details(),
                collectionsLinkBuilder.self(),
                collectionsLinkBuilder.next(collections.pageInfo)
            )
        )
    }

    @GetMapping("/{id}")
    fun show(@PathVariable("id") id: String): Resource<CollectionResource> {
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
        val links = mutableListOf(collectionsLinkBuilder.self())

        if (collection.isOwnedByCurrentUser()) {
            links.add(
                linkTo(
                    methodOn(CollectionsController::class.java).patchCollection(id = collection.id, request = null)
                ).withRel("edit")
            )
            links.add(
                linkTo(
                    methodOn(CollectionsController::class.java).removeCollection(id = collection.id)
                ).withRel("remove")
            )

            links.add(
                linkTo(
                    methodOn(CollectionsController::class.java).addVideo(collectionId = collection.id, videoId = null)
                ).withRel("addVideo")
            )

            links.add(
                linkTo(
                    methodOn(CollectionsController::class.java).removeVideo(
                        collectionId = collection.id,
                        videoId = null
                    )
                ).withRel("removeVideo")
            )
        }

        return Resource(collection, links)
    }
}

private fun CollectionResource.isOwnedByCurrentUser() = this.owner == getCurrentUserId().value