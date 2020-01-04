package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.video.AdminSearchRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.RateVideoRequest
import com.boclips.videos.api.request.video.TagVideoRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.RateVideo
import com.boclips.videos.service.application.video.ShareVideo
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.ValidateWithShareCode
import com.boclips.videos.service.application.video.VideoTranscriptService
import com.boclips.videos.service.application.video.exceptions.InvalidShareCodeException
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.video.SortKey
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.presentation.converters.VideoToResourceConverter
import com.boclips.videos.service.presentation.hateoas.HateoasEmptyCollection
import com.boclips.videos.service.presentation.projections.WithProjection
import com.boclips.videos.service.presentation.support.Cookies
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
import javax.validation.Valid

@RestController
@RequestMapping("/v1/videos")
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
    private val shareVideo: ShareVideo,
    private val validateWithShareCode: ValidateWithShareCode,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService) {
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
        @RequestParam(name = "subject", required = false) subjects: Set<String>?,
        @RequestParam(name = "subjects_set_manually", required = false) subjectsSetManually: Boolean?,
        @RequestParam(name = "promoted", required = false) promoted: Boolean?,
        @RequestParam(name = "content_partner", required = false) contentPartners: Set<String>?,
        @RequestParam(name = "type", required = false) type: Set<String>?
    ): ResponseEntity<PagedResources<*>> {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_INDEX
        val videos = searchVideo.byQuery(
            query = query,
            sortBy = sortBy,
            includeTags = includeTags?.let { includeTags } ?: emptyList(),
            excludeTags = excludeTags?.let { excludeTags } ?: emptyList(),
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
            user = getCurrentUser()
        )

        return ResponseEntity(
            PagedResources(
                videos
                    .elements.toList()
                    .map { video -> videoToResourceConverter.fromVideo(video, getCurrentUser()) }
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
    fun adminSearch(@RequestBody adminSearchRequest: AdminSearchRequest?): ResponseEntity<Resources<*>> {
        val user = getCurrentUser()
        return searchVideo.byIds(adminSearchRequest?.ids ?: emptyList(), user)
            .map { videoToResourceConverter.fromVideo(it, user) }
            .let(HateoasEmptyCollection::fixIfEmptyCollection)
            .let { ResponseEntity(Resources(it), HttpStatus.CREATED) }
    }

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
            withProjection(
                searchVideo.byId(id, getCurrentUser())
                    .let { videoToResourceConverter.fromVideo(it, getCurrentUser()) }
            ),
            headers,
            HttpStatus.OK
        )
    }

    @GetMapping(path = ["/{id}/match"], params = ["shareCode"])
    fun validateShareCode(@PathVariable("id") id: String?, @RequestParam shareCode: String? = null): ResponseEntity<Void> {
        shareCode?.let {
            try {
                validateWithShareCode(id!!, shareCode)
            } catch (e: InvalidShareCodeException) {
                return ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun removeVideo(@PathVariable("id") id: String?) {
        deleteVideo(id, getCurrentUser())
    }

    @GetMapping("/{id}/transcript")
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

    @PostMapping
    fun postVideo(@RequestBody @Valid createVideoRequest: CreateVideoRequest): ResponseEntity<Any> {
        val resource = try {
            createVideo(createVideoRequest, getCurrentUser())
                .let { videoToResourceConverter.fromVideo(it, getCurrentUser()) }
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

        return ResponseEntity(HttpHeaders().apply {
            set(HttpHeaders.LOCATION, resource.getLink("self").href)
        }, HttpStatus.CREATED)
    }

    @PatchMapping(path = ["/{id}"], params = ["rating"])
    fun patchRating(@RequestParam rating: Int?, @PathVariable id: String) =
        rateVideo(
            rateVideoRequest = RateVideoRequest(rating = rating, videoId = id),
            user = getCurrentUser()
        ).let { this.getVideo(id) }

    @PatchMapping("/{id}", params = ["sharing=true"])
    fun patchSharing(@PathVariable("id") id: String, @RequestParam sharing: Boolean): ResponseEntity<Void> {
        return if (sharing) {
            shareVideo(id, getCurrentUser())
            ResponseEntity(HttpHeaders(), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    @PatchMapping(path = ["/{id}"], params = ["!rating"])
    fun patchVideo(
        @PathVariable id: String,
        @RequestParam subjectIds: List<String>? = emptyList(), //TODO: move to updateRequest if the spring gods allow it
        updateRequest: UpdateVideoRequest
    ): ResponseEntity<MappingJacksonValue> {
        val updateRequestWithSubjects = updateRequest.copy(subjectIds = subjectIds)

        return updateVideo(id, updateRequestWithSubjects, getCurrentUser()).let { this.getVideo(id) }
    }

    @PatchMapping(path = ["/{id}/tags"])
    fun patchTag(@PathVariable id: String, @RequestBody tagUrl: String?) =
        tagVideo(TagVideoRequest(id, tagUrl), getCurrentUser()).let { this.getVideo(id) }
}

