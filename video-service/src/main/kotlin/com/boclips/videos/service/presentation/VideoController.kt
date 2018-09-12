package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.SearchVideos
import com.boclips.videos.service.presentation.resources.VideoResource
import com.boclips.videos.service.presentation.resources.resourcesOf
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/v1/videos")
class VideoController(private val searchVideos: SearchVideos) {
    companion object {
        fun searchLink() = linkTo(methodOn(VideoController::class.java).search(null)).withRel("search")
    }

    @GetMapping
    fun search(@RequestParam("query") query: String?): Resources<*> {
        val videos = searchVideos.execute(query)
        return resourcesOf(videos, VideoResource::class).apply { add(searchLink()) }
    }
}
