package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.service.application.collection.GetCollectionsOfUser
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.converters.CollectionResourceConverter
import com.boclips.videos.service.presentation.projections.WithProjection
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users/{id}/collections")
class UserCollectionsController(
    private val getCollectionOfUser: GetCollectionsOfUser,
    private val collectionResourceConverter: CollectionResourceConverter,
    private val withProjection: WithProjection,
    accessRuleService: AccessRuleService,
    getUserIdOverride: GetUserIdOverride,
    userService: UserService
) : BaseController(accessRuleService, getUserIdOverride, userService) {

    @GetMapping
    fun getMyCollections(
        @PathVariable("id") collectionOwnerId: String,
        collectionFilterRequest: CollectionFilterRequest
    ): ResponseEntity<MappingJacksonValue> {
        val user = getCurrentUser()

        if (!user.isAuthenticated) {
            throw OperationForbiddenException("User must be authenticated")
        }

        val collections: ResultsPage<Collection, Nothing> = getCollectionOfUser(
            owner = UserId(collectionOwnerId), request = collectionFilterRequest, requester = user
        )

        val collectionsResource = collectionResourceConverter.buildCollectionsResource(
            collections,
            getCurrentUser(),
            collectionFilterRequest.projection
        )

        val payload = withProjection(collectionsResource)

        return ResponseEntity(payload, HttpStatus.OK)
    }
}
