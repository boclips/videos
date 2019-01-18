package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.collection.AddVideoToDefaultCollection
import com.boclips.videos.service.application.collection.GetDefaultCollection
import com.boclips.videos.service.presentation.collections.CollectionResource
import mu.KLogging
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/collections")
class CollectionsController(
        private val getDefaultCollection: GetDefaultCollection,
        private val addVideoToDefaultCollection: AddVideoToDefaultCollection
) {
    companion object : KLogging() {
        fun getUserDefaultCollectionLink() = linkTo(methodOn(CollectionsController::class.java).getDefaultCollection())
    }

    @GetMapping("/default")
    fun getDefaultCollection(): Resource<CollectionResource> {
        val selfLink = getUserDefaultCollectionLink().withSelfRel()
        val addVideoLink = linkTo(methodOn(CollectionsController::class.java)
                .addVideo(null))
                .withRel("addVideo")
        val collectionResource = getDefaultCollection.execute()
        return Resource(collectionResource, selfLink, addVideoLink)
    }

    @PutMapping("/default/videos/{video_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addVideo(@PathVariable("video_id") videoId: String?): Any? {
        addVideoToDefaultCollection.execute(videoId)
        return null
    }
}