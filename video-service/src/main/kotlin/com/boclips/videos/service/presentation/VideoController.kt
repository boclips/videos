package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideoTranscript
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.exceptions.VideoExists
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.SortKey
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/videos")
class VideoController(
    private val searchVideo: SearchVideo,
    private val deleteVideos: DeleteVideos,
    private val createVideo: CreateVideo,
    private val updateVideo: UpdateVideo,
    private val bulkUpdateVideo: BulkUpdateVideo,
    private val getVideoTranscript: GetVideoTranscript,
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500
        const val DEFAULT_PAGE_INDEX = 0
    }

    @GetMapping
    fun search(
        @RequestParam("query") query: String?,
        @RequestParam(name = "sort_by", required = false) sortBy: SortKey?,
        @RequestParam(name = "include_tag", required = false) includeTags: List<String>?,
        @RequestParam(name = "exclude_tag", required = false) excludeTags: List<String>?,
        @RequestParam(name = "min_duration", required = false) minDuration: String?,
        @RequestParam(name = "max_duration", required = false) maxDuration: String?,
        @RequestParam(name = "released_date_from", required = false) releasedDateFrom: String?,
        @RequestParam(name = "released_date_to", required = false) releasedDateTo: String?,
        @RequestParam(name = "source", required = false) source: String?,
        @RequestParam("size") size: Int?,
        @RequestParam("page") page: Int?
    ): ResponseEntity<PagedResources<*>> {
        val videosResource = searchVideo.byQuery(
            query = query,
            sortBy = sortBy,
            includeTags = includeTags?.let { includeTags } ?: emptyList(),
            excludeTags = excludeTags?.let { excludeTags } ?: emptyList(),
            releasedDateFrom = releasedDateFrom,
            releasedDateTo = releasedDateTo,
            pageSize = size ?: DEFAULT_PAGE_SIZE,
            pageNumber = page ?: DEFAULT_PAGE_INDEX,
            minDuration = minDuration,
            maxDuration = maxDuration,
            source = source
        )

        val videoResources = videosResource
            .videos
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
            .let(HateoasEmptyCollection::fixIfEmptyCollection)
            .let { ResponseEntity(Resources(it), HttpStatus.CREATED) }
    }

    @GetMapping("/{id}")
    fun getVideo(@PathVariable("id") id: String?): Resource<VideoResource> {
        return searchVideo.byId(id)
    }

    @DeleteMapping("/{id}")
    fun deleteVideo(@PathVariable("id") id: String?) {
        deleteVideos(id)
    }

    @GetMapping("/{id}/transcript")
    fun getTranscript(@PathVariable("id") id: String?): ResponseEntity<String> {
        val videoTranscript = getVideoTranscript(id)
        val videoTitle = searchVideo.byId(id).content.title!!.replace(Regex("""[/\\\\?%\\*:\\|"<>\\. ]"""), "_")

        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$videoTitle.txt\"")

        return ResponseEntity(videoTranscript, headers, HttpStatus.OK)
    }

    @PostMapping
    fun postVideo(@RequestBody createVideoRequest: CreateVideoRequest): ResponseEntity<Any> {
        val resource = try {
            createVideo(createVideoRequest)
        } catch (e: VideoExists) {
            val errorDetails =
                mapOf("error" to "video from provider \"${e.contentPartnerId}\" and provider id \"${e.contentPartnerVideoId}\" already exists")
            return ResponseEntity(errorDetails, HttpStatus.CONFLICT)
        } catch (e: Exception) {
            val errorDetails = mapOf("error" to e.message, "processed request" to createVideoRequest)
            logger.error(objectMapper.writeValueAsString(errorDetails))
            return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
        }

        val headers = HttpHeaders()
        headers.set(HttpHeaders.LOCATION, resource.getLink("self").href)
        return ResponseEntity(headers, HttpStatus.CREATED)
    }

    @PostMapping("/{id}")
    fun patchOneVideo(@PathVariable("id") id: String?, @RequestBody patchVideoRequest: VideoResource): ResponseEntity<Void> {
        updateVideo(id, patchVideoRequest)
        return ResponseEntity(HttpHeaders(), HttpStatus.NO_CONTENT)
    }

    @PatchMapping
    fun patchMultipleVideos(@RequestBody bulkUpdateRequest: BulkUpdateRequest?): ResponseEntity<Void> {
        bulkUpdateVideo(bulkUpdateRequest)
        return ResponseEntity(HttpHeaders(), HttpStatus.NO_CONTENT)
    }
}
