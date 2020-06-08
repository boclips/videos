package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.presentation.converters.CollectionResourceConverter
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.projections.WithProjection
import mu.KLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/collections")
class CollectionsController(
    private val createCollection: CreateCollection,
    private val getCollections: GetCollections,
    private val collectionsLinkBuilder: CollectionsLinkBuilder,
    private val collectionResourceConverter: CollectionResourceConverter,
    private val withProjection: WithProjection,
    accessRuleService: AccessRuleService,
    getUserIdOverride: GetUserIdOverride
) : BaseController(accessRuleService, getUserIdOverride) {
    companion object : KLogging() {
        const val COLLECTIONS_PAGE_SIZE = 30
    }

    @GetMapping
    fun searchCollections(collectionFilterRequest: CollectionFilterRequest): ResponseEntity<MappingJacksonValue> {
        val user = getCurrentUser()

        if (!user.isAuthenticated) {
            throw OperationForbiddenException("User must be authenticated to access collections")
        }

        val collections: ResultsPage<Collection, Nothing> = getCollections(collectionFilterRequest, user)
        val collectionsResource = collectionResourceConverter.buildCollectionsResource(
            collections,
            getCurrentUser(),
            collectionFilterRequest.projection
        )
        val payload = withProjection(collectionsResource)

        return ResponseEntity(payload, HttpStatus.OK)
    }

    @PostMapping
    fun postCollection(@Valid @RequestBody createCollectionRequest: CreateCollectionRequest): ResponseEntity<MappingJacksonValue> {
        val collection = createCollection(createCollectionRequest, getCurrentUser())
        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, collectionsLinkBuilder.collection(collection.id.value)?.href)
        }

        val collectionsResource = collectionResourceConverter.buildCollectionResource(
            collection,
            Projection.details,
            getCurrentUser()
        )

        val payload = withProjection(collectionsResource)

        return ResponseEntity(payload, headers, HttpStatus.CREATED)
    }
}
