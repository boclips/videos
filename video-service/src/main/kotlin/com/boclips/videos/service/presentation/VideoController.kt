package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.video.AdminSearchRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.RateVideoRequest
import com.boclips.videos.api.request.video.TagVideoRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.api.response.video.VideosWrapperResource
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.RateVideo
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.VideoTranscriptService
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.SortKey
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.presentation.converters.VideoToResourceConverter
import com.boclips.videos.service.presentation.projections.WithProjection
import com.boclips.videos.service.presentation.support.Cookies
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.hateoas.PagedResources
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
    private val objectMapper: ObjectMapper,
    private val withProjection: WithProjection,
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
    fun search(
        @RequestParam(name = "query", required = false) query: String?,
        @RequestParam(name = "sort_by", required = false) sortBy: SortKey?,
        @RequestParam(name = "best_for", required = false) bestFor: List<String>?,
        @RequestParam(name = "duration_min", required = false) minDuration: String?,
        @RequestParam(name = "duration_max", required = false) maxDuration: String?,
        @RequestParam(name = "released_date_from", required = false) releasedDateFrom: String?,
        @RequestParam(name = "released_date_to", required = false) releasedDateTo: String?,
        @RequestParam(name = "source", required = false) source: String?,
        @RequestParam(name = "age_range_min", required = false) ageRangeMin: Int?,
        @RequestParam(name = "age_range_max", required = false) ageRangeMax: Int?,
        @RequestParam(name = "size", required = false) size: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "subject", required = false) subjects: Set<String>?,
        @RequestParam(name = "subjects_set_manually", required = false) subjectsSetManually: Boolean?,
        @RequestParam(name = "promoted", required = false) promoted: Boolean?,
        @RequestParam(name = "content_partner", required = false) contentPartners: Set<String>?,
        @RequestParam(name = "type", required = false) type: Set<String>?,
        @RequestParam(name = "is_classroom", required = false) isClassroom: Boolean?
    ): ResponseEntity<VideosResource> {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_INDEX
        val videos = searchVideo.byQuery(
            query = query,
            sortBy = sortBy,
            bestFor = bestFor?.let { bestFor } ?: emptyList(),
            minDuration = minDuration,
            maxDuration = maxDuration,
            releasedDateFrom = releasedDateFrom,
            releasedDateTo = releasedDateTo,
            pageSize = pageSize,
            pageNumber = pageNumber,
            source = source,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            subjects = subjects ?: emptySet(),
            subjectsSetManually = subjectsSetManually,
            promoted = promoted,
            contentPartnerNames = contentPartners ?: emptySet(),
            type = type?.let { type } ?: emptySet(),
            isClassroom = isClassroom,
            user = getCurrentUser()
        )

        val videosResource = VideosResource(
            _embedded = VideosWrapperResource(videos = videos
                .elements.toList()
                .map { video -> videoToResourceConverter.convertVideo(video, getCurrentUser()) }),
            _links = null,
            page = PagedResources.PageMetadata(
                pageSize.toLong(),
                pageNumber.toLong(),
                videos.pageInfo.totalElements
            )
        )

        return ResponseEntity(videosResource, HttpStatus.OK)
    }

    @PostMapping("/v1/videos/search")
    fun adminSearch(@RequestBody adminSearchRequest: AdminSearchRequest?): ResponseEntity<VideosResource> {
        val user = getCurrentUser()
        return searchVideo.byIds(adminSearchRequest?.ids ?: emptyList(), user)
            .map { videoToResourceConverter.convertVideo(it, user) }
            .let {
                ResponseEntity(
                    VideosResource(_embedded = VideosWrapperResource(videos = it), _links = null, page = null),
                    HttpStatus.CREATED
                )
            }
    }

    @CrossOrigin(allowCredentials = "true")
    @GetMapping(path = ["/v1/videos/{id}"])
    fun getVideo(@PathVariable("id") id: String?, @CookieValue(Cookies.PLAYBACK_DEVICE) playbackConsumer: String? = null): ResponseEntity<MappingJacksonValue> {
        val headers = HttpHeaders()
        if (playbackConsumer == null) {
            headers.add(
                "Set-Cookie",
                "${Cookies.PLAYBACK_DEVICE}=${UUID.randomUUID()}; Max-Age=31536000; Path=/; HttpOnly; SameSite=None; Secure"
            )
        }

        return ResponseEntity(
            withProjection(
                searchVideo.byId(id, getCurrentUser())
                    .let { videoToResourceConverter.convertVideo(it, getCurrentUser()) }
            ),
            headers,
            HttpStatus.OK
        )
    }

    @DeleteMapping("/v1/videos/{id}")
    fun removeVideo(@PathVariable("id") id: String?) {
        deleteVideo(id, getCurrentUser())
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

    @PostMapping("/v1/videos")
    fun postVideo(@RequestBody @Valid createVideoRequest: CreateVideoRequest): ResponseEntity<VideoResource> {
        val resource: VideoResource = try {
            createVideo(createVideoRequest, getCurrentUser())
                .let { videoToResourceConverter.convertVideo(it, getCurrentUser()) }
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
    fun patchRating(@RequestParam rating: Int?, @PathVariable id: String) =
        rateVideo(
            rateVideoRequest = RateVideoRequest(rating = rating, videoId = id),
            user = getCurrentUser()
        ).let { this.getVideo(id) }

    @PatchMapping(path = ["/v1/videos/{id}"], params = ["!rating"])
    fun patchVideo(
        @PathVariable id: String,
        @RequestParam subjectIds: List<String>? = emptyList(), //TODO: move to updateRequest if the spring gods allow it
        updateRequest: UpdateVideoRequest
    ): ResponseEntity<MappingJacksonValue> {
        val updateRequestWithSubjects = updateRequest.copy(subjectIds = subjectIds)

        return updateVideo(id, updateRequestWithSubjects, getCurrentUser()).let { this.getVideo(id) }
    }

    @PatchMapping(path = ["/v1/videos/{id}/tags"])
    fun patchTag(@PathVariable id: String, @RequestBody tagUrl: String?) =
        tagVideo(TagVideoRequest(id, tagUrl), getCurrentUser()).let { this.getVideo(id) }

    @RequestMapping(
        "/v1/content-partners/{contentPartnerId}/videos/{contentPartnerVideoId}",
        method = [RequestMethod.HEAD]
    )
    fun lookupVideoByProviderId(
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
}

