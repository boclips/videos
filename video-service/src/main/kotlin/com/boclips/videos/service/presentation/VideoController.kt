package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.RateVideo
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.VideoTranscriptService
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.video.SortKey
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.projections.WithProjection
import com.boclips.videos.service.presentation.video.AdminSearchRequest
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.RateVideoRequest
import com.boclips.videos.service.presentation.video.TagVideoRequest
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.hateoas.PagedResources
import org.springframework.hateoas.Resources
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/videos")
class VideoController(
    private val searchVideo: SearchVideo,
    private val deleteVideo: DeleteVideo,
    private val createVideo: CreateVideo,
    private val bulkUpdateVideo: BulkUpdateVideo,
    private val updateVideo: UpdateVideo,
    private val rateVideo: RateVideo,
    private val videoTranscriptService: VideoTranscriptService,
    private val objectMapper: ObjectMapper,
    private val withProjection: WithProjection,
    private val tagVideo: TagVideo,
    private val videoToResourceConverter: VideoToResourceConverter
) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500
        const val DEFAULT_PAGE_INDEX = 0
    }

    @GetMapping
    fun search(
        @RequestParam(name = "query", required = false) query: String?,
        @RequestParam(name = "sort_by", required = false) sortBy: SortKey?,
        @RequestParam(name = "include_tag", required = false) includeTags: List<String>?,
        @RequestParam(name = "exclude_tag", required = false) excludeTags: List<String>?,
        @RequestParam(name = "duration_min", required = false) minDuration: String?,
        @RequestParam(name = "duration_max", required = false) maxDuration: String?,
        @RequestParam(name = "released_date_from", required = false) releasedDateFrom: String?,
        @RequestParam(name = "released_date_to", required = false) releasedDateTo: String?,
        @RequestParam(name = "source", required = false) source: String?,
        @RequestParam(name = "age_range_min", required = false) ageRangeMin: Int?,
        @RequestParam(name = "age_range_max", required = false) ageRangeMax: Int?,
        @RequestParam(name = "size", required = false) size: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "subject", required = false) subjects: Set<String>?
    ): ResponseEntity<PagedResources<*>> {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_INDEX
        val videos = searchVideo.byQuery(
            query = query,
            sortBy = sortBy,
            includeTags = includeTags?.let { includeTags } ?: emptyList(),
            excludeTags = excludeTags?.let { excludeTags } ?: emptyList(),
            releasedDateFrom = releasedDateFrom,
            releasedDateTo = releasedDateTo,
            pageSize = pageSize,
            pageNumber = pageNumber,
            minDuration = minDuration,
            maxDuration = maxDuration,
            source = source,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            subjects = subjects ?: emptySet()
        )

        return ResponseEntity(
            PagedResources(
                videos
                    .elements.toList()
                    .map(videoToResourceConverter::fromVideo)
                    .let(HateoasEmptyCollection::fixIfEmptyCollection),
                PagedResources.PageMetadata(
                    pageSize.toLong(),
                    pageNumber.toLong(),
                    videos.pageInfo.totalElements
                )
            ), HttpStatus.OK
        )
    }

    @PostMapping("/search")
    fun adminSearch(@RequestBody adminSearchRequest: AdminSearchRequest?): ResponseEntity<Resources<*>> =
        searchVideo.byIds(adminSearchRequest?.ids ?: emptyList())
            .map(videoToResourceConverter::fromVideo)
            .let(HateoasEmptyCollection::fixIfEmptyCollection)
            .let { ResponseEntity(Resources(it), HttpStatus.CREATED) }

    @CrossOrigin(allowCredentials = "true")
    @GetMapping(path = ["/{id}"])
    fun getVideo(@PathVariable("id") id: String?, @CookieValue(Cookies.PLAYBACK_DEVICE) playbackConsumer: String? = null): ResponseEntity<MappingJacksonValue> {
        val headers = HttpHeaders()
        if (playbackConsumer == null) {
            headers.add(
                "Set-Cookie",
                "${Cookies.PLAYBACK_DEVICE}=${UUID.randomUUID()}; Max-Age=31536000; Path=/; HttpOnly; SameSite=None; Secure"
            )
        }

        return ResponseEntity(
            withProjection(videoToResourceConverter.fromVideo(searchVideo.byId(id))),
            headers,
            HttpStatus.OK
        )
    }

    @DeleteMapping("/{id}")
    fun removeVideo(@PathVariable("id") id: String?) {
        deleteVideo(id)
    }

    @GetMapping("/{id}/transcript")
    fun getTranscript(@PathVariable("id") videoId: String?): ResponseEntity<String> {
        val videoTranscript = videoTranscriptService.getTranscript(videoId)
        val videoTitle = searchVideo.byId(videoId).title.replace(Regex("""[/\\\\?%\\*:\\|"<>\\. ]"""), "_")

        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$videoTitle.txt\"")

        return ResponseEntity(videoTranscript, headers, HttpStatus.OK)
    }

    @PostMapping
    fun postVideo(@RequestBody createVideoRequest: CreateVideoRequest): ResponseEntity<Any> {
        val resource = try {
            val video = createVideo(createVideoRequest)

            videoToResourceConverter.fromVideo(video)
        } catch (e: VideoAssetAlreadyExistsException) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    "Error creating video",
                    "video from provider \"${e.contentPartnerId}\" and provider id \"${e.contentPartnerVideoId}\" already exists",
                    HttpStatus.CONFLICT
                )
            )
        } catch (e: Exception) {
            logger.error(
                objectMapper.writeValueAsString(
                    mapOf(
                        "error" to e.message,
                        "processed request" to createVideoRequest
                    )
                )
            )
            throw InvalidRequestApiException(
                ExceptionDetails(
                    "Error creating video",
                    e.message.orEmpty(),
                    HttpStatus.BAD_REQUEST
                )
            )
        }

        return ResponseEntity(HttpHeaders().apply {
            set(HttpHeaders.LOCATION, resource.getLink("self").href)
        }, HttpStatus.CREATED)
    }

    @PatchMapping
    fun patchMultipleVideos(@RequestBody bulkUpdateRequest: BulkUpdateRequest?): ResponseEntity<Void> {
        bulkUpdateVideo(bulkUpdateRequest)
        return ResponseEntity(HttpHeaders(), HttpStatus.NO_CONTENT)
    }

    @PatchMapping(path = ["/{id}"], params = ["rating"])
    fun patchRating(@RequestParam rating: Int?, @PathVariable id: String) =
        rateVideo(rateVideoRequest = RateVideoRequest(rating = rating, videoId = id)).let { this.getVideo(id) }

    @PatchMapping(path = ["/{id}"], params = ["!rating"])
    fun patchVideo(@PathVariable id: String, @RequestParam(required = false) title: String?, @RequestParam(required = false) description: String?) =
        updateVideo(id, title, description).let { this.getVideo(id) }

    @PatchMapping(path = ["/{id}/tags"])
    fun patchTag(@PathVariable id: String, @RequestBody tagUrl: String?) =
        tagVideo(TagVideoRequest(id, tagUrl)).let { this.getVideo(id) }
}
