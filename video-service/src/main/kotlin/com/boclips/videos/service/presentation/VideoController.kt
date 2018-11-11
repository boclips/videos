package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideoById
import com.boclips.videos.service.application.video.GetVideosByQuery
import com.boclips.videos.service.infrastructure.logging.SearchLogging
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/videos")
class VideoController(
        private val getVideoById: GetVideoById,
        private val getVideosByQuery: GetVideosByQuery,
        private val deleteVideos: DeleteVideos
) {
    companion object {
        fun searchLink() = linkTo(methodOn(VideoController::class.java).search(null)).withRel("search")
        fun getVideoLink(id: String? = null, rel: String = "asset") = linkTo(methodOn(VideoController::class.java).getVideo(id)).withRel(rel)
    }

    @GetMapping
    @SearchLogging
    fun search(@RequestParam("query") query: String?): ResponseEntity<Resources<*>> {
        val results = getVideosByQuery.execute(query)
                .map(this::videoToResource)
                .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return ResponseEntity(Resources(results), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getVideo(@PathVariable("id") id: String?): Resource<VideoResource> {
        val video = getVideoById.execute(id)

        return videoToResource(video)
    }

    private fun videoToResource(videoResource: VideoResource) =
            Resource(videoResource, getVideoLink(videoResource.id, "self"))

    @DeleteMapping("/{id}")
    fun deleteVideo(@PathVariable("id") id: String?) {
        deleteVideos.execute(id)
    }
}
