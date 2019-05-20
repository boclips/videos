package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CollectionFilter
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import mu.KLogging
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
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
import javax.validation.Valid

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
    private val bookmarkCollection: BookmarkCollection,
    private val unbookmarkCollection: UnbookmarkCollection,
    private val collectionsLinkBuilder: CollectionsLinkBuilder
) {
    companion object : KLogging() {
        const val PUBLIC_COLLECTIONS_PAGE_SIZE = 30
    }

    @GetMapping
    fun getFilteredCollections(
        @RequestParam projection: Projection,
        @RequestParam public: Boolean = false,
        @RequestParam bookmarked: Boolean = false,
        @RequestParam(required = false) owner: String?,
        @RequestParam page: Int?,
        @RequestParam size: Int?
    ): Resources<*> {
        val collectionFilter = CollectionFilter(
            projection = projection,
            visibility = when {
                bookmarked -> CollectionFilter.Visibility.BOOKMARKED
                public -> CollectionFilter.Visibility.PUBLIC
                else -> CollectionFilter.Visibility.PRIVATE
            },
            owner = owner,
            pageNumber = page?.let { it } ?: 0,
            pageSize = size?.let { it } ?: PUBLIC_COLLECTIONS_PAGE_SIZE
        )

        val collections = getCollections(collectionFilter)

        val collectionResources = collections.elements.map(::wrapCollection)
            .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return Resources(
            collectionResources,
            listOfNotNull(
                collectionsLinkBuilder.projections().list(),
                collectionsLinkBuilder.projections().details(),
                collectionsLinkBuilder.self(),
                collectionsLinkBuilder.next(collections.pageInfo)
            )
        )
    }

    @PostMapping
    fun postCollection(@RequestBody createCollectionRequest: CreateCollectionRequest?): ResponseEntity<Void> {
        val collection = createCollection(createCollectionRequest)
        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, collectionsLinkBuilder.collection(collection.id.value)?.href)
        }
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    @PatchMapping("/{id}")
    fun patchCollection(@PathVariable("id") id: String, @Valid @RequestBody request: UpdateCollectionRequest?): ResponseEntity<Resource<*>> {
        updateCollection(id, request)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PatchMapping("/{id}", params = ["bookmarked=true"])
    fun patchBookmarkCollection(@PathVariable("id") id: String): Resource<CollectionResource> {
        bookmarkCollection(id)
        return this.show(id)
    }

    @PatchMapping("/{id}", params = ["bookmarked=false"])
    fun patchUnbookmarkCollection(@PathVariable("id") id: String): Resource<CollectionResource> {
        unbookmarkCollection(id)
        return this.show(id)
    }

    @DeleteMapping("/{id}")
    fun removeCollection(@PathVariable("id") id: String): ResponseEntity<Void> {
        deleteCollection(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
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

    private fun wrapCollection(collection: CollectionResource) =
        Resource(
            collection, listOfNotNull(
                collectionsLinkBuilder.self(),
                collectionsLinkBuilder.editCollection(collection),
                collectionsLinkBuilder.removeCollection(collection),
                collectionsLinkBuilder.addVideoToCollection(collection),
                collectionsLinkBuilder.removeVideoFromCollection(collection),
                collectionsLinkBuilder.bookmark(collection),
                collectionsLinkBuilder.unbookmark(collection)
            )
        )
}
