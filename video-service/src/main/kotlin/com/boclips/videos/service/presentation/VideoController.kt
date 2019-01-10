package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.*
import com.boclips.videos.service.infrastructure.logging.SearchLogging
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResource
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.hateoas.PagedResources
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.RuntimeException

@RestController
@RequestMapping("/v1/videos")
class VideoController(
        private val getVideoById: GetVideoById,
        private val getVideosByQuery: GetVideosByQuery,
        private val deleteVideos: DeleteVideos,
        private val createVideo: CreateVideo,
        private val patchVideo: PatchVideo,
        private val objectMapper: ObjectMapper
) {
    companion object : KLogging() {
        fun getSearchLink() = linkTo(methodOn(VideoController::class.java).search(null, null, null, null, null)).withRel("search")
        fun getVideoLink(id: String? = null, rel: String = "video") = linkTo(methodOn(VideoController::class.java).getVideo(id)).withRel(rel)

        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500
        const val DEFAULT_PAGE_INDEX = 0
    }

    @GetMapping
    @SearchLogging
    fun search(@RequestParam("query") query: String?,
               @RequestParam(name = "include_tag", required = false) includeTags: List<String>?,
               @RequestParam(name = "exclude_tag", required = false) excludeTags: List<String>?,
               @RequestParam("size") size: Int?,
               @RequestParam("page") page: Int?): ResponseEntity<PagedResources<*>> {
        val videosResource = getVideosByQuery.execute(query = query,
                includeTags = includeTags?.let { includeTags } ?: emptyList(),
                excludeTags = excludeTags?.let { excludeTags } ?: emptyList(),
                pageSize = size ?: DEFAULT_PAGE_SIZE,
                pageNumber = page ?: DEFAULT_PAGE_INDEX)

        val videoResources = videosResource
                .videos
                .map(this::wrapResourceWithHateoas)
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
        val videoResource = getVideoById.execute(id)

        return wrapResourceWithHateoas(videoResource)
    }

    @DeleteMapping("/{id}")
    fun deleteVideo(@PathVariable("id") id: String?) {
        deleteVideos.execute(id)
    }

    @PostMapping
    fun createVideo(@RequestBody createVideoRequest: CreateVideoRequest): ResponseEntity<Any> {
        val resource = try {
            createVideo.execute(createVideoRequest)
        } catch (e: Exception) {
            val errorDetails = mapOf("error" to e.message, "processed request" to createVideoRequest)
            logger.error(objectMapper.writeValueAsString(errorDetails))
            return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
        }

        val headers = HttpHeaders()
        headers.set(HttpHeaders.LOCATION, getVideoLink(resource.id, "self").href)
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    @PostMapping("/{id}")
    fun patchVideo(@PathVariable("id") id: String?, @RequestBody patchVideoRequest: VideoResource): ResponseEntity<Void> {
        patchVideo.execute(id, patchVideoRequest)
        return ResponseEntity(HttpHeaders(), HttpStatus.NO_CONTENT)
    }

    private fun wrapResourceWithHateoas(videoResource: VideoResource) =
            Resource(videoResource, getVideoLink(videoResource.id, "self"))
}
