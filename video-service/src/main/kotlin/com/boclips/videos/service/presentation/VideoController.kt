package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideoById
import com.boclips.videos.service.application.video.GetVideosByQuery
import com.boclips.videos.service.infrastructure.logging.SearchLogging
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.PagedResources
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/videos")
class VideoController(
        private val getVideoById: GetVideoById,
        private val getVideosByQuery: GetVideosByQuery,
        private val deleteVideos: DeleteVideos,
        private val createVideo: CreateVideo
) {
    companion object {
        fun getSearchLink() = linkTo(methodOn(VideoController::class.java).search(null, null, null)).withRel("search")
        fun getVideoLink(id: String? = null, rel: String = "video") = linkTo(methodOn(VideoController::class.java).getVideo(id)).withRel(rel)

        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500
        const val DEFAULT_PAGE_INDEX = 0
    }


    @GetMapping
    @SearchLogging
    fun search(@RequestParam("query") query: String?,
               @RequestParam("pageSize") pageSize: Int?,
               @RequestParam("pageNumber") pageNumber: Int?): ResponseEntity<PagedResources<*>> {
        val videosResource = getVideosByQuery.execute(query = query,
                pageNumber = pageNumber ?: DEFAULT_PAGE_INDEX,
                pageSize = pageSize ?: DEFAULT_PAGE_SIZE)

        val videoResources = videosResource
                .videos
                .map(this::videoToResource)
                .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return ResponseEntity(
                PagedResources(
                        videoResources,
                        PagedResources.PageMetadata(
                                videosResource.pageSize.toLong(),
                                videosResource.pageNumber.toLong(),
                                videosResource.totalVideos
                        )
                ), HttpStatus.OK)
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

    @PostMapping
    fun createVideo(@RequestBody createVideoRequest: CreateVideoRequest): ResponseEntity<Void> {

        val assetId = createVideo.execute(createVideoRequest)

        val headers = HttpHeaders()
        headers.set(HttpHeaders.LOCATION, getVideoLink(assetId.value, "self").href)
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    private fun videoToResource(videoResource: VideoResource) =
            Resource(videoResource, getVideoLink(videoResource.id, "self"))
}
