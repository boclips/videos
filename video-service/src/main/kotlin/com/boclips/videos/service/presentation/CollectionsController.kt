package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CollectionFilter
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.GetViewerCollections
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.projections.WithProjection
import mu.KLogging
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJacksonValue
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
    private val collectionsLinkBuilder: CollectionsLinkBuilder,
    private val withProjection: WithProjection,
    private val getViewerCollections: GetViewerCollections
) {
    companion object : KLogging() {
        const val COLLECTIONS_PAGE_SIZE = 30
    }

    @GetMapping
    fun getFilteredCollections(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) projection: Projection?,
        @RequestParam(required = false) public: Boolean = false,
        @RequestParam(required = false) bookmarked: Boolean = false,
        @RequestParam(required = false) owner: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) subject: List<String>?
    ): MappingJacksonValue {
        val collectionFilter = CollectionFilter(
            query = query,
            projection = projection ?: Projection.list,
            visibility = when {
                bookmarked -> CollectionFilter.Visibility.BOOKMARKED
                public || query != null -> CollectionFilter.Visibility.PUBLIC
                else -> CollectionFilter.Visibility.PRIVATE
            },
            owner = owner,
            pageNumber = page?.let { it } ?: 0,
            pageSize = size?.let { it } ?: COLLECTIONS_PAGE_SIZE,
            subjects = subject ?: emptyList()
        )

        val collectionsPage = getCollections(collectionFilter)

        val collectionResources = collectionsPage.elements.map(::wrapCollection)
            .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return withProjection(
            Resources(
                collectionResources,
                listOfNotNull(
                    collectionsLinkBuilder.projections().list(),
                    collectionsLinkBuilder.projections().details(),
                    collectionsLinkBuilder.self(),
                    collectionsLinkBuilder.next(collectionsPage.pageInfo)
                )
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
    fun patchCollection(@PathVariable("id") id: String, @Valid @RequestBody request: UpdateCollectionRequest?): ResponseEntity<Void> {
        updateCollection(id, request)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PatchMapping("/{id}", params = ["bookmarked=true"])
    fun patchBookmarkCollection(@PathVariable("id") id: String): MappingJacksonValue {
        bookmarkCollection(id)
        return this.show(id)
    }

    @PatchMapping("/{id}", params = ["bookmarked=false"])
    fun patchUnbookmarkCollection(@PathVariable("id") id: String): MappingJacksonValue {
        unbookmarkCollection(id)
        return this.show(id)
    }

    @DeleteMapping("/{id}")
    fun removeCollection(@PathVariable("id") id: String): ResponseEntity<Void> {
        deleteCollection(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    // TODO Drop this ASAP
    @GetMapping("/dont-do-this-at-home")
    fun dontDoThisAtHome(): MappingJacksonValue {
        val viewerCollections = getViewerCollections(getCurrentUserId())

        val collectionResources = viewerCollections.elements.map(::wrapCollection)
            .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return withProjection(
            Resources(
                collectionResources,
                listOfNotNull(
                    collectionsLinkBuilder.self(),
                    collectionsLinkBuilder.next(viewerCollections.pageInfo)
                )
            )
        )
    }

    @GetMapping("/{id}")
    fun show(
        @PathVariable("id") id: String,
        @RequestParam(required = false) projection: Projection? = Projection.list
    ) = withProjection(wrapCollection(getCollection(id, projection ?: Projection.list)))

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
                collectionsLinkBuilder.self(collection),
                collectionsLinkBuilder.editCollection(collection),
                collectionsLinkBuilder.removeCollection(collection),
                collectionsLinkBuilder.addVideoToCollection(collection),
                collectionsLinkBuilder.removeVideoFromCollection(collection),
                collectionsLinkBuilder.bookmark(collection),
                collectionsLinkBuilder.unbookmark(collection)
            )
        )
}
