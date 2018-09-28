package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.video.SearchVideos
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/v1/videos")
class VideoController(private val searchVideos: SearchVideos) {
    companion object {
        fun searchLink() = linkTo(methodOn(VideoController::class.java).search(null)).withRel("search")
        fun videoLink(id: String? = null, rel: String = "video") = linkTo(methodOn(VideoController::class.java).getVideo(id)).withRel(rel)
    }

    @GetMapping("/search")
    fun search(@RequestParam("query") query: String?): Resource<SearchResponse> {
        val results = searchVideos.execute(query)
        return Resource(SearchResponse(searchId = results.searchId, query = results.query, videos = results.videos.map { toVideoResource(it) }))
    }

    @GetMapping("/{id}")
    fun getVideo(@PathVariable("id") id: String?): Resource<VideoResource> {
        val video = searchVideos.get(id!!)
        return toVideoResource(video)
    }

    fun toVideoResource(video: VideoResource) =
            Resource(video, videoLink(video.id, "self"))
}
