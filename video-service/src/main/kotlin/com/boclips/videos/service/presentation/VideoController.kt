package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.video.*
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.service.application.video.*
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import com.boclips.videos.service.domain.model.video.request.SortKey
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.presentation.converters.VideoToResourceConverter
import com.boclips.videos.service.presentation.support.Cookies
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid

@RestController
class VideoController(
    private val searchVideo: SearchVideo,
    private val deleteVideo: DeleteVideo,
    private val createVideo: CreateVideo,
    private val updateVideo: UpdateVideo,
    private val rateVideo: RateVideo,
    private val videoTranscriptService: VideoTranscriptService,
    private val videoCaptionService: VideoCaptionService,
    private val updateCaptionContent: UpdateCaptionContent,
    private val objectMapper: ObjectMapper,
    private val tagVideo: TagVideo,
    private val videoToResourceConverter: VideoToResourceConverter,
    private val videoRepository: VideoRepository,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500
        const val DEFAULT_PAGE_INDEX = 0
    }

    @GetMapping("/v1/videos")
    fun getVideos(
        @RequestParam(name = "query", required = false) query: String?,
        @RequestParam(name = "sort_by", required = false) sortBy: SortKey?,
        @RequestParam(name = "best_for", required = false) bestFor: List<String>?,
        @RequestParam(name = "duration_min", required = false) minDuration: String?,
        @RequestParam(name = "duration_max", required = false) maxDuration: String?,
        @RequestParam(name = "duration", required = false) duration: List<String>?,
        @RequestParam(name = "duration_facets", required = false) durationFacets: List<String>?,
        @RequestParam(name = "released_date_from", required = false) releasedDateFrom: String?,
        @RequestParam(name = "released_date_to", required = false) releasedDateTo: String?,
        @RequestParam(name = "source", required = false) source: String?,
        @RequestParam(name = "age_range_min", required = false) ageRangeMin: Int?,
        @RequestParam(name = "age_range_max", required = false) ageRangeMax: Int?,
        @RequestParam(name = "age_range", required = false) ageRanges: List<String>?,
        @RequestParam(name = "age_range_facets", required = false) ageRangeFacets: List<String>?,
        @RequestParam(name = "size", required = false) size: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "subject", required = false) subjects: Set<String>?,
        @RequestParam(name = "subjects_set_manually", required = false) subjectsSetManually: Boolean?,
        @RequestParam(name = "promoted", required = false) promoted: Boolean?,
        @RequestParam(name = "content_partner", required = false) contentPartners: Set<String>?,
        @RequestParam(name = "type", required = false) type: Set<String>?,
        @RequestParam(name = "id", required = false) ids: Set<String>?
    ): ResponseEntity<VideosResource> {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_INDEX
        val results = searchVideo.byQuery(
            query = query,
            ids = ids ?: emptySet(),
            bestFor = bestFor?.let { bestFor } ?: emptyList(),
            minDuration = minDuration,
            maxDuration = maxDuration,
            duration = duration,
            durationFacets = durationFacets,
            releasedDateFrom = releasedDateFrom,
            releasedDateTo = releasedDateTo,
            source = source,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            ageRanges = ageRanges.orEmpty(),
            ageRangeFacets = ageRangeFacets.orEmpty(),
            subjects = subjects ?: emptySet(),
            subjectsSetManually = subjectsSetManually,
            promoted = promoted,
            contentPartnerNames = contentPartners ?: emptySet(),
            type = type?.let { type } ?: emptySet(),
            user = getCurrentUser(),
            sortBy = sortBy,
            pageSize = pageSize,
            pageNumber = pageNumber
        )

        val videosResource = videoToResourceConverter.convert(resultsPage = results, user = getCurrentUser())

        return ResponseEntity(videosResource, HttpStatus.OK)
    }

    @CrossOrigin(allowCredentials = "true")
    @GetMapping("/v1/videos/{id}")
    fun getVideo(
        @PathVariable("id") id: String?,
        @CookieValue(Cookies.PLAYBACK_DEVICE) playbackConsumer: String? = null,
        @RequestParam(required = false) projection: Projection? = null
    ): ResponseEntity<VideoResource> {
        val headers = HttpHeaders()
        if (playbackConsumer == null) {
            headers.add(
                "Set-Cookie",
                "${Cookies.PLAYBACK_DEVICE}=${UUID.randomUUID()}; Max-Age=31536000; Path=/; HttpOnly; SameSite=None; Secure"
            )
        }

        val resources: VideoResource = searchVideo.byId(id, getCurrentUser())
            .let { videoToResourceConverter.convert(it, getCurrentUser()) }
            .let {
                when (projection) {
                    Projection.full -> videoCaptionService.withCaptionDetails(it)
                    else -> it
                }

            }

        return ResponseEntity(resources, headers, HttpStatus.OK)
    }

    @GetMapping("/v1/videos/{id}/transcript")
    fun getTranscript(@PathVariable("id") videoId: String?): ResponseEntity<String> {
        val videoTitle =
            searchVideo.byId(videoId, getCurrentUser()).title.replace(Regex("""[/\\\\?%\\*:\\|"<>\\. ]"""), "_")

        val videoTranscript: String = videoTranscriptService.getTranscript(videoId).let {
            if (it.contains(Regex("\\n\\n"))) {
                it
            } else {
                it.replace(Regex("\\. "), ".\n\n")
            }
        }

        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$videoTitle.txt\"")

        return ResponseEntity(videoTranscript, headers, HttpStatus.OK)
    }

    @PostMapping("/v1/videos/{id}/transcript")
    fun updateTranscript(@PathVariable("id") videoId: String?, @RequestBody updateCaptionRequest: UpdateVideoCaptionsRequest): ResponseEntity<String> {

        updateCaptionContent(videoId!!, updateCaptionRequest.transcript!!)
        return ResponseEntity(HttpStatus.OK)
    }

    @RequestMapping(
        path = ["/v1/content-partners/{contentPartnerId}/videos/{contentPartnerVideoId}"],
        method = [RequestMethod.HEAD]
    )
    fun getVideoByProviderId(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @PathVariable("contentPartnerVideoId") contentPartnerVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromContentPartnerId(
            ContentPartnerId(value = contentPartnerId),
            contentPartnerVideoId
        )

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }

    @DeleteMapping("/v1/videos/{id}")
    fun removeVideo(@PathVariable("id") id: String?) {
        deleteVideo(id, getCurrentUser())
    }

    @PostMapping("/v1/videos")
    fun postCreateVideo(@RequestBody @Valid createVideoRequest: CreateVideoRequest): ResponseEntity<VideoResource> {
        val resource: VideoResource = try {
            createVideo(createVideoRequest)
                .let { videoToResourceConverter.convert(it, getCurrentUser()) }
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
                ),
                e
            )
            throw InvalidRequestApiException(
                ExceptionDetails(
                    "Error creating video",
                    e.message.orEmpty(),
                    HttpStatus.BAD_REQUEST
                )
            )
        }

        return ResponseEntity(resource, HttpHeaders().apply {
            set(HttpHeaders.LOCATION, resource._links?.get("self")?.href)
        }, HttpStatus.CREATED)
    }

    @PatchMapping(path = ["/v1/videos/{id}"], params = ["rating"])
    fun patchRating(@RequestParam(required = true) rating: Int?, @PathVariable id: String) =
        rateVideo(
            rateVideoRequest = RateVideoRequest(rating = rating, videoId = id),
            user = getCurrentUser()
        ).let { this.getVideo(id) }

    @PatchMapping(path = ["/v1/videos/{id}"], params = ["!rating"])
    fun patchUpdateVideo(
        @PathVariable id: String,
        @RequestBody updateRequest: UpdateVideoRequest
    ): ResponseEntity<VideoResource> {
        return updateVideo(id, updateRequest, getCurrentUser()).let { this.getVideo(id) }
    }

    @PatchMapping(path = ["/v1/videos/{id}/tags"])
    fun patchUpdateTag(@PathVariable id: String, @RequestBody tagUrl: String?) =
        tagVideo(TagVideoRequest(id, tagUrl), getCurrentUser()).let { this.getVideo(id) }
}
