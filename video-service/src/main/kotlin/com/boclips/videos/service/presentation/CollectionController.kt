package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.application.video.VideoRetrievalService
import com.boclips.videos.service.infrastructure.user.GetUserOrganisationAndExternalId
import com.boclips.videos.service.presentation.converters.CollectionResourceConverter
import com.boclips.videos.service.presentation.projections.WithProjection
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/collections")
class CollectionController(
        private val getCollection: GetCollection,
        private val addVideoToCollection: AddVideoToCollection,
        private val removeVideoFromCollection: RemoveVideoFromCollection,
        private val updateCollection: UpdateCollection,
        private val deleteCollection: DeleteCollection,
        private val bookmarkCollection: BookmarkCollection,
        private val unbookmarkCollection: UnbookmarkCollection,
        private val collectionResourceConverter: CollectionResourceConverter,
        private val videoRetrievalService: VideoRetrievalService,
        private val withProjection: WithProjection,
        accessRuleService: AccessRuleService,
        getUserOrganisationAndExternalId: GetUserOrganisationAndExternalId
) : BaseController(accessRuleService, getUserOrganisationAndExternalId) {
    @PatchMapping("/{id}")
    fun patchCollection(
        @PathVariable("id") id: String,
        @Valid @RequestBody request: UpdateCollectionRequest?
    ): ResponseEntity<Void> {
        updateCollection(id, request, getCurrentUser())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PatchMapping("/{id}", params = ["bookmarked=true"])
    fun patchBookmarkCollection(@PathVariable("id") id: String): MappingJacksonValue {
        val user = getCurrentUser()
        bookmarkCollection(id, user)
        return getCollection.withoutPopulatingVideos(collectionId = id, user = user).let { collection ->
            withProjection(collectionResourceConverter.buildCollectionListResource(collection, user))
        }
    }

    @PatchMapping("/{id}", params = ["bookmarked=false"])
    fun patchUnbookmarkCollection(@PathVariable("id") id: String): MappingJacksonValue {
        val user = getCurrentUser()
        unbookmarkCollection(id, user)
        return getCollection.withoutPopulatingVideos(collectionId = id, user = user).let { collection ->
            withProjection(collectionResourceConverter.buildCollectionListResource(collection, user))
        }
    }

    @DeleteMapping("/{id}")
    fun removeCollection(@PathVariable("id") id: String): ResponseEntity<Void> {
        deleteCollection(id, getCurrentUser())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{id}")
    fun show(
        @PathVariable("id") id: String,
        @RequestParam(required = false) projection: Projection? = Projection.list,
        @RequestParam(name = "referer", required = false) referer: String? = null,
        @RequestParam(name = "shareCode", required = false) shareCode: String? = null
    ): MappingJacksonValue {
        val user = getCurrentUser()
        val collection =
            getCollection(collectionId = id, user = user, referer = referer, shareCode = shareCode)

        val collectionResource = when (projection) {
            Projection.details -> collectionResourceConverter.buildCollectionDetailsResource(
                collection,
                user,
                videoRetrievalService.getPlayableVideos(collection.videos, user.accessRules.videoAccess)
            )
            else -> collectionResourceConverter.buildCollectionListResource(collection, user)
        }

        return withProjection(collectionResource)
    }

    @PutMapping("/{collection_id}/videos/{video_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addVideo(
        @PathVariable("collection_id") collectionId: String?,
        @PathVariable("video_id") videoId: String?
    ): Any? {
        addVideoToCollection(collectionId = collectionId, videoId = videoId, user = getCurrentUser())
        return null
    }

    @DeleteMapping("/{collection_id}/videos/{video_id}")
    fun removeVideo(
        @PathVariable("collection_id") collectionId: String?,
        @PathVariable("video_id") videoId: String?
    ): ResponseEntity<Void> {
        removeVideoFromCollection(collectionId, videoId, getCurrentUser())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
