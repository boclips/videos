package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.SearchVideos
import com.boclips.videos.service.presentation.resources.VideoResource
import com.boclips.videos.service.presentation.resources.resourcesOf
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/v1/videos")
class VideoController(private val searchVideos: SearchVideos) {
    companion object {
        fun searchLink() = linkTo(methodOn(VideoController::class.java).search(null)).withRel("search")
        fun videoLink(id: String? = null, rel: String = "video") = linkTo(methodOn(VideoController::class.java).video(id)).withRel(rel)
    }

    @GetMapping
    fun search(@RequestParam("query") query: String?): Resources<*> {
        val videos = searchVideos.execute(query)
        return resourcesOf(videos.map { toVideoResource(it) }, VideoResource::class).apply { add(searchLink()) }
    }

    @GetMapping("/{id}")
    fun video(@PathVariable("id") id: String?): Resource<VideoResource> {
        val video = searchVideos.get(id!!)
        return toVideoResource(video)
    }

    fun toVideoResource(video: VideoResource) =
            Resource(video, videoLink(video.id, "self"))
}
