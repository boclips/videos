package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.PatchVideo
import com.boclips.videos.service.application.video.exceptions.VideoAssetExists
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.video.AdminSearchRequest
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResource
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.hateoas.PagedResources
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/videos")
class VideoController(
    private val searchVideo: SearchVideo,
    private val deleteVideos: DeleteVideos,
    private val createVideo: CreateVideo,
    private val patchVideo: PatchVideo,
    private val bulkUpdateVideo: BulkUpdateVideo,
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging() {
        fun searchLink() = linkTo(
            methodOn(VideoController::class.java)
                .search(null, null, null, null, null)
        ).withRel("search")

        fun videoLink(id: String? = null, rel: String = "video") = linkTo(
            methodOn(VideoController::class.java)
                .getVideo(id)
        ).withRel(rel)

        fun videosLink() = linkTo(methodOn(VideoController::class.java).patchMultipleVideos(null)).withRel("videos")

        fun adminSearchLink() = linkTo(methodOn(VideoController::class.java).adminSearch(null)).withRel("adminSearch")

        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500
        const val DEFAULT_PAGE_INDEX = 0
    }

    @GetMapping
    fun search(
        @RequestParam("query") query: String?,
        @RequestParam(name = "include_tag", required = false) includeTags: List<String>?,
        @RequestParam(name = "exclude_tag", required = false) excludeTags: List<String>?,
        @RequestParam("size") size: Int?,
        @RequestParam("page") page: Int?
    ): ResponseEntity<PagedResources<*>> {
        val videosResource = searchVideo.byQuery(query = query,
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
            ), HttpStatus.OK
        )
    }

    @PostMapping("/search")
    fun adminSearch(@RequestBody adminSearchRequest: AdminSearchRequest?): ResponseEntity<Resources<*>> {
        return searchVideo.byIds(adminSearchRequest?.ids ?: emptyList())
            .map(this::wrapResourceWithHateoas)
            .let(HateoasEmptyCollection::fixIfEmptyCollection)
            .let { ResponseEntity(Resources(it), HttpStatus.CREATED) }
    }

    @GetMapping("/{id}")
    fun getVideo(@PathVariable("id") id: String?): Resource<VideoResource> {
        val videoResource = searchVideo.byId(id)

        return wrapResourceWithHateoas(videoResource)
    }

    @DeleteMapping("/{id}")
    fun deleteVideo(@PathVariable("id") id: String?) {
        deleteVideos(id)
    }

    @PostMapping
    fun postVideo(@RequestBody createVideoRequest: CreateVideoRequest): ResponseEntity<Any> {
        val resource = try {
            createVideo(createVideoRequest)
        } catch (e: VideoAssetExists) {
            val errorDetails =
                mapOf("error" to "video from provider \"${e.contentPartnerId}\" and provider id \"${e.contentPartnerVideoId}\" already exists")
            return ResponseEntity(errorDetails, HttpStatus.CONFLICT)
        } catch (e: Exception) {
            val errorDetails = mapOf("error" to e.message, "processed request" to createVideoRequest)
            logger.error(objectMapper.writeValueAsString(errorDetails))
            return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
        }

        val headers = HttpHeaders()
        headers.set(HttpHeaders.LOCATION, videoLink(resource.id, "self").href)
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    @PostMapping("/{id}")
    fun patchOneVideo(@PathVariable("id") id: String?, @RequestBody patchVideoRequest: VideoResource): ResponseEntity<Void> {
        patchVideo(id, patchVideoRequest)
        return ResponseEntity(HttpHeaders(), HttpStatus.NO_CONTENT)
    }

    @PatchMapping
    fun patchMultipleVideos(@RequestBody bulkUpdateRequest: BulkUpdateRequest?): ResponseEntity<Void> {
        bulkUpdateVideo(bulkUpdateRequest)
        return ResponseEntity(HttpHeaders(), HttpStatus.NO_CONTENT)
    }

    private fun wrapResourceWithHateoas(videoResource: VideoResource) =
        Resource(videoResource, videoLink(videoResource.id, "self"))
}
