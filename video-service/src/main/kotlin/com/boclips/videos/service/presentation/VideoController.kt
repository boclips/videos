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
        fun getSearchLink() = linkTo(methodOn(VideoController::class.java).search(null, null, null)).withRel("search")
        fun getVideoLink(id: String? = null, rel: String = "video") = linkTo(methodOn(VideoController::class.java).getVideo(id)).withRel(rel)
    }

    @GetMapping
    @SearchLogging
    fun search(@RequestParam("query") query: String?,
               @RequestParam("pageSize") pageSize: Int?,
               @RequestParam("pageNumber") pageNumber: Int?): ResponseEntity<Resources<*>> {
        val results = getVideosByQuery.execute(query = query, pageIndex = pageNumber, pageSize = pageSize)
                .videos
                .map(this::videoToResource)
                .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return ResponseEntity(Resources(results), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getVideo(@PathVariable("id") id: String?): Resource<VideoResource> {
        val video = getVideoById.execute(id)

        return videoToResource(video)
    }

    @DeleteMapping("/{id}")
    fun deleteVideo(@PathVariable("id") id: String?) {
        deleteVideos.execute(id)
    }

    private fun videoToResource(videoResource: VideoResource) =
            Resource(videoResource, getVideoLink(videoResource.id, "self"))
}
