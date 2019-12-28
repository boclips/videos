package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.projections.Projection
import com.boclips.videos.service.presentation.projections.WithProjection
import mu.KLogging
import org.springframework.hateoas.PagedResources
import org.springframework.hateoas.Resource
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
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService) {
    companion object : KLogging() {
        const val COLLECTIONS_PAGE_SIZE = 30
    }

    data class CollectionsRequest(
        val query: String? = null,
        val public: Boolean? = null,
        val bookmarked: Boolean? = null,
        val owner: String? = null,
        val page: Int? = null,
        val size: Int? = null,
        val projection: Projection = Projection.list,
        val hasLessonPlans: Boolean? = null,
        private val subject: String? = null
    ) {
        val subjects = subject?.split(",") ?: emptyList()
    }

    @GetMapping
    fun getFilteredCollections(
        collectionsRequest: CollectionsRequest
    ): MappingJacksonValue {
        val user = getCurrentUser()

        if (!user.isAuthenticated) {
            throw OperationForbiddenException("User must be authenticated to access collections")
        }

        val collectionsPage = getCollections(collectionsRequest, user)

        val collectionResources = collectionsPage.elements.map(::wrapCollection)
            .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return withProjection(
            PagedResources(
                collectionResources,
                PagedResources.PageMetadata(
                    collectionsPage.pageInfo.pageRequest.size.toLong(),
                    collectionsPage.pageInfo.pageRequest.page.toLong(),
                    collectionsPage.pageInfo.totalElements,
                    collectionsPage.pageInfo.totalElements / collectionsPage.pageInfo.pageRequest.size.toLong()
                ),
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
    fun postCollection(@Valid @RequestBody createCollectionRequest: CreateCollectionRequest): ResponseEntity<Void> {
        val collection = createCollection(createCollectionRequest, getCurrentUser())
        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, collectionsLinkBuilder.collection(collection.id.value)?.href)
        }
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    @PatchMapping("/{id}")
    fun patchCollection(@PathVariable("id") id: String, @Valid @RequestBody request: UpdateCollectionRequest?): ResponseEntity<Void> {
        updateCollection(id, request, getCurrentUser())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PatchMapping("/{id}", params = ["bookmarked=true"])
    fun patchBookmarkCollection(@PathVariable("id") id: String): MappingJacksonValue {
        bookmarkCollection(id, getCurrentUser())
        return this.show(id)
    }

    @PatchMapping("/{id}", params = ["bookmarked=false"])
    fun patchUnbookmarkCollection(@PathVariable("id") id: String): MappingJacksonValue {
        unbookmarkCollection(id, getCurrentUser())
        return this.show(id)
    }

    @DeleteMapping("/{id}")
    fun removeCollection(@PathVariable("id") id: String): ResponseEntity<Void> {
        deleteCollection(id, getCurrentUser())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{id}")
    fun show(
        @PathVariable("id") id: String,
        @RequestParam(required = false) projection: Projection? = Projection.list
    ) = withProjection(wrapCollection(getCollection(id, projection ?: Projection.list, getCurrentUser())))

    @PutMapping("/{collection_id}/videos/{video_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addVideo(@PathVariable("collection_id") collectionId: String?, @PathVariable("video_id") videoId: String?): Any? {
        addVideoToCollection(collectionId = collectionId, videoId = videoId, requester = getCurrentUser())

        return null
    }

    @DeleteMapping("/{collection_id}/videos/{video_id}")
    fun removeVideo(@PathVariable("collection_id") collectionId: String?, @PathVariable("video_id") videoId: String?): ResponseEntity<Void> {
        removeVideoFromCollection(collectionId, videoId, getCurrentUser())
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
                collectionsLinkBuilder.unbookmark(collection),
                collectionsLinkBuilder.interactedWith(collection)
            )
        )
}
