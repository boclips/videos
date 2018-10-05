package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideos
import com.boclips.videos.service.infrastructure.search.event.SearchLogging
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
        private val getVideos: GetVideos,
        private val deleteVideos: DeleteVideos
) {
    companion object {
        fun searchLink() = linkTo(methodOn(VideoController::class.java).search(null)).withRel("search")
        fun getVideoLink(id: String? = null, rel: String = "video") = linkTo(methodOn(VideoController::class.java).getVideo(id)).withRel(rel)
        fun deleteVideoLink(id: String? = null, rel: String = "delete") = linkTo(methodOn(VideoController::class.java).deleteVideo(id)).withRel(rel)
    }

    @GetMapping("/search")
    @SearchLogging
    fun search(@RequestParam("query") query: String?): ResponseEntity<Resources<Resource<VideoResource>>> {
        val results = getVideos.get(query)

        return ResponseEntity(Resources(results), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getVideo(@PathVariable("id") id: String?): Resource<VideoResource> {
        val video = getVideos.get(id!!)

        return Resource(video, getVideoLink(video.id, "self"))
    }

    @DeleteMapping("/{id}")
    fun deleteVideo(@PathVariable("id") id: String?) {
        deleteVideos.delete(id)
    }
}
