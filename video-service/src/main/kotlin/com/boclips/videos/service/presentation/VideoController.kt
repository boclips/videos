package com.boclips.videos.service.presentation

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.video.CaptionFormatRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.MetadataRequest
import com.boclips.videos.api.request.video.RateVideoRequest
import com.boclips.videos.api.request.video.SetThumbnailRequest
import com.boclips.videos.api.request.video.TagVideoRequest
import com.boclips.videos.api.request.video.UpdateVideoCaptionsRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.video.CaptionsResource
import com.boclips.videos.api.response.video.PriceResource
import com.boclips.videos.api.response.video.VideoMetadataResponse
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoUrlAssetsResource
import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.service.application.collection.exceptions.InvalidWebVTTException
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.DeleteVideoThumbnail
import com.boclips.videos.service.application.video.GetVideoUrlAssets
import com.boclips.videos.service.application.video.RateVideo
import com.boclips.videos.service.application.video.SetVideoThumbnail
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.application.video.TagVideosCsv
import com.boclips.videos.service.application.video.UpdateCaptionContent
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.UploadThumbnailImageToVideo
import com.boclips.videos.service.application.video.VideoCaptionService
import com.boclips.videos.service.application.video.VideoTranscriptService
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.search.GetVideoPrice
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.playback.CaptionConflictException
import com.boclips.videos.service.domain.model.video.UnsupportedFormatConversionException
import com.boclips.videos.service.domain.model.video.UnsupportedVideoUpdateException
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.request.SortKey
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.presentation.converters.CaptionFormatRequestEnumConverter
import com.boclips.videos.service.presentation.converters.PriceConverter
import com.boclips.videos.service.presentation.converters.QueryParamsConverter
import com.boclips.videos.service.presentation.converters.VideoMetadataConverter
import com.boclips.videos.service.presentation.converters.VideoToResourceConverter
import com.boclips.videos.service.presentation.converters.videoTagging.VideoTaggingCsvFileValidator
import com.boclips.videos.service.presentation.exceptions.InvalidVideoPaginationException
import com.boclips.videos.service.presentation.exceptions.InvalidVideoTaggingCsvFile
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.servlet.ServletRequest
import javax.validation.Valid

@RestController
class VideoController(
    private val searchVideo: SearchVideo,
    private val deleteVideo: DeleteVideo,
    private val createVideo: CreateVideo,
    private val updateVideo: UpdateVideo,
    private val rateVideo: RateVideo,
    private val setVideoThumbnail: SetVideoThumbnail,
    private val deleteVideoThumbnail: DeleteVideoThumbnail,
    private val uploadThumbnailImageToVideo: UploadThumbnailImageToVideo,
    private val videoTranscriptService: VideoTranscriptService,
    private val videoCaptionService: VideoCaptionService,
    private val updateCaptionContent: UpdateCaptionContent,
    private val objectMapper: ObjectMapper,
    private val tagVideo: TagVideo,
    private val videoToResourceConverter: VideoToResourceConverter,
    private val videoRepository: VideoRepository,
    private val videosLinkBuilder: VideosLinkBuilder,
    private val getVideoUrlAssets: GetVideoUrlAssets,
    private val videosRepository: VideoRepository,
    private val getVideoPrice: GetVideoPrice,
    private val tagVideosCsv: TagVideosCsv,
    private val videoMetadataConverter: VideoMetadataConverter,
    private val videoTaggingCsvFileValidator: VideoTaggingCsvFileValidator,
    val userService: UserService,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService,
) : BaseController(accessRuleService, getUserIdOverride, userService) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500
        const val DEFAULT_PAGE_INDEX = 0
        const val MAXIMUM_SEARCH_RESULT_WINDOW_SIZE = 10000
    }

    // For converting an lowercase caption format request enum
    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(
            CaptionFormatRequest::class.java,
            CaptionFormatRequestEnumConverter()
        )
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
        @RequestParam(name = "channel", required = false) channelParam: Set<String>?,
        @RequestParam(name = "include_channel_facets", required = false) includeChannelFacets: Boolean?,
        @RequestParam(name = "type", required = false) type: Set<String>?,
        @RequestParam(name = "video_type_facets", required = false) videoTypeFacets: List<String>?,
        @RequestParam(name = "id", required = false) ids: Set<String>?,
        @RequestParam(name = "resource_types", required = false) resourceTypes: Set<String>?,
        @RequestParam(name = "resource_type_facets", required = false) resourceTypeFacets: List<String>?,
        @RequestParam(name = "prices", required = false) prices: Set<String>?,
        @RequestParam(name = "category_code", required = false) categoryCode: Set<String>?,
        @RequestParam(name = "updated_as_of", required = false) updatedAsOf: String?,
        request: ServletRequest
    ): ResponseEntity<VideosResource> {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_INDEX

        val requestedSearchWindow = pageSize * (pageNumber + 1)
        if (requestedSearchWindow > MAXIMUM_SEARCH_RESULT_WINDOW_SIZE) {
            throw InvalidVideoPaginationException(
                message = "Requested page is too deep. Maximum supported window is $MAXIMUM_SEARCH_RESULT_WINDOW_SIZE but was $requestedSearchWindow"
            )
        }

        if (pageSize > MAX_PAGE_SIZE) {
            throw InvalidVideoPaginationException(
                message = "Requested page size is too big. Maximum supported page size is $MAX_PAGE_SIZE"
            )
        }

        val user = getCurrentUser()
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
            resourceTypes = resourceTypes ?: emptySet(),
            resourceTypeFacets = resourceTypeFacets.orEmpty(),
            videoTypeFacets = videoTypeFacets.orEmpty(),
            promoted = promoted,
            channelIds = channelParam ?: emptySet(),
            type = type?.let { type } ?: emptySet(),
            user = user,
            sortBy = sortBy,
            pageSize = pageSize,
            pageNumber = pageNumber,
            includeChannelFacets = includeChannelFacets,
            includePriceFacets = UserExtractor.currentUserHasRole(UserRoles.BOCLIPS_WEB_APP),
            queryParams = QueryParamsConverter.toSplitList(request.parameterMap),
            prices = prices?.map { PriceConverter.toPrice(it) }?.toSet() ?: emptySet(),
            categoryCodes = categoryCode.orEmpty(),
            updatedAsOf = parseDateAsZonedDateTime(updatedAsOf)
        )

        val videosResource = videoToResourceConverter.convert(resultsPage = results, user = user)

        return ResponseEntity(videosResource, HttpStatus.OK)
    }

    @CrossOrigin(originPatterns = ["*"], allowCredentials = "true")
    @GetMapping("/v1/videos/{id}")
    fun getVideo(
        @PathVariable("id") id: String?,
        @RequestParam(required = false) projection: Projection? = null,
        @RequestParam(required = false) referer: String? = null,
        @RequestParam(required = false) shareCode: String? = null,
        @RequestParam(required = false) userId: String? = null
    ): ResponseEntity<VideoResource> {
        val video = searchVideo.byId(id, getCurrentUser(), projection)

        val canAccessProtectedAttributes = when {
            getCurrentUser().isAuthenticated -> true
            shareCode == null || referer == null -> true
            else -> userService.isShareCodeValid(referer, shareCode)
        }

        if (video.deactivated) {
            return ResponseEntity(
                HttpHeaders().apply {
                    set(HttpHeaders.LOCATION, videosLinkBuilder.self(video.activeVideoId?.value).href)
                },
                HttpStatus.PERMANENT_REDIRECT
            )
        }
        return video
            .let {
                videoToResourceConverter.convert(
                    it,
                    user = getCurrentUser(),
                    omitProtectedAttributes = !canAccessProtectedAttributes
                )
            }
            .let {
                when (projection) {
                    Projection.full -> videoCaptionService.withCaptionDetails(it)
                    else -> it
                }
            }
            .let { ResponseEntity(it, HttpStatus.OK) }
    }

    @GetMapping("/v1/videos/{id}/transcript")
    fun getTranscript(@PathVariable("id") videoId: String?): ResponseEntity<String> {
        val videoTitle = getVideoTitle(videoId)

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

    @RequestMapping(
        path = ["/v1/content-partners/{contentPartnerId}/videos/{contentPartnerVideoId}"],
        method = [RequestMethod.HEAD]
    )
    fun getVideoByProviderId(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @PathVariable("contentPartnerVideoId") contentPartnerVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromChannelId(
            ChannelId(value = contentPartnerId),
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
            val user = getCurrentUser()

            createVideo(createVideoRequest, user)
                .let {
                    videoToResourceConverter.convert(it, user)
                }
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

        return ResponseEntity(
            resource,
            HttpHeaders().apply {
                set(HttpHeaders.LOCATION, resource._links?.get("self")?.href)
            },
            HttpStatus.CREATED
        )
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
        try {
            return updateVideo(id, updateRequest, getCurrentUser()).let { this.getVideo(id) }
        } catch (e: UnsupportedVideoUpdateException) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    "Error updating video",
                    e.message ?: "Cannot update video with ID \"${id}\" ",
                    HttpStatus.BAD_REQUEST
                )
            )
        }
    }

    @PatchMapping(path = ["/v1/videos/{id}/playback"], params = ["thumbnailSecond"])
    fun setThumbnailBySecond(
        @RequestParam thumbnailSecond: Int?,
        @PathVariable id: String
    ): ResponseEntity<VideoResource> {
        return setVideoThumbnail(
            SetThumbnailRequest.SetThumbnailBySecond(
                videoId = id,
                thumbnailSecond = thumbnailSecond
            )
        ).let { this.getVideo(id) }
    }

    @PostMapping(path = ["/v1/videos/{id}/playback"], params = ["playbackId"])
    fun setCustomThumbnail(
        @RequestParam playbackId: String?,
        @RequestParam("thumbnailImage") thumbnailImage: MultipartFile?,
        @PathVariable id: String
    ): ResponseEntity<VideoResource> {
        return uploadThumbnailImageToVideo(
            videoId = id,
            playbackId = playbackId,
            imageStream = thumbnailImage?.inputStream,
            filename = thumbnailImage?.originalFilename
        ).let { this.getVideo(id) }
    }

    @DeleteMapping(path = ["/v1/videos/{id}/playback/thumbnail"])
    fun deleteManuallySetThumbnail(@PathVariable id: String): ResponseEntity<VideoResource> {
        return deleteVideoThumbnail(id).let { this.getVideo(id) }
    }

    @PatchMapping(path = ["/v1/videos/{id}/tags"])
    fun patchUpdateTag(@PathVariable id: String, @RequestBody tagUrl: String?) =
        tagVideo(TagVideoRequest(id, tagUrl), getCurrentUser()).let { this.getVideo(id) }

    @PutMapping("/v1/videos/{id}/captions")
    fun updateCaptions(
        @PathVariable("id") videoId: String?,
        @RequestBody updateCaptionRequest: UpdateVideoCaptionsRequest
    ): ResponseEntity<String> {
        try {
            updateCaptionContent(videoId!!, updateCaptionRequest.captions!!)
        } catch (exception: InvalidWebVTTException) {
            throw InvalidRequestApiException(
                ExceptionDetails("WebVTT file provided is invalid", exception.message.orEmpty(), HttpStatus.BAD_REQUEST)
            )
        } catch (e: Exception) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    "Error updating captions",
                    e.message.orEmpty(),
                    HttpStatus.BAD_REQUEST
                )
            )
        }
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping(value = ["/v1/videos/{id}/captions"], params = ["generated=true"])
    fun generateCaptions(
        @PathVariable("id") videoId: String?
    ): ResponseEntity<String> {
        try {
            videoCaptionService.requestCaptionIfMissing(videoId)
        } catch (ex: CaptionConflictException) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/v1/videos/{id}/captions")
    fun getCaptions(
        @PathVariable("id") videoId: String?,
        @RequestParam("download") shouldDownload: Boolean = false,
        @RequestParam("human-generated") useHumanGeneratedOnly: Boolean = false,
        @RequestParam("format") captionFormat: CaptionFormatRequest? = null
    ): ResponseEntity<Any> {
        try {
            val caption = videoCaptionService.getCaption(videoId!!, useHumanGeneratedOnly, captionFormat)
            return caption?.let {
                if (shouldDownload) {
                    val headers = HttpHeaders()
                    headers.set(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"${getVideoTitle(videoId)}.${it.format.getFileExtension()}\""
                    )

                    ResponseEntity(it.content, headers, HttpStatus.OK)
                } else {
                    ResponseEntity(CaptionsResource(it.content), HttpStatus.OK)
                }
            } ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (ex: UnsupportedFormatConversionException) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    "Error converting caption for video",
                    ex.message,
                    HttpStatus.BAD_REQUEST
                )
            )
        }
    }

    private fun getVideoTitle(videoId: String?) =
        searchVideo.byId(videoId, getCurrentUser()).title.replace(Regex("""[/\\\\?%\\*:\\|"<>\\. ]"""), "_").trim()

    @GetMapping("/v1/videos/{id}/assets")
    fun getAssets(@PathVariable("id") videoId: String): ResponseEntity<VideoUrlAssetsResource> {
        val assets = getVideoUrlAssets(videoId, getCurrentUser())

        return ResponseEntity.ok(assets)
    }

    // TODO: it's a temporary solution only for christmas time 🎅🌲🎅🌲🎅🌲🎅
    @PostMapping("/v1/videos/metadata")
    fun getMetadata(@Valid @RequestBody metadataRequest: MetadataRequest?): ResponseEntity<VideoMetadataResponse> {
        val videoIds = metadataRequest!!.ids.map { VideoId(it) }

        val videos = videosRepository.findAll(videoIds)

        val videoToCaptionLinkMap =
            videos.associate { it.videoId.value to videoCaptionService.getCaption(it.videoId.value, true, null) }
        val videosResource = videoToResourceConverter.convert(videos, getCurrentUser())

        val convertVideosToRequiredMetadata = videoMetadataConverter.convert(videosResource, videoToCaptionLinkMap)
        val response = VideoMetadataResponse(convertVideosToRequiredMetadata)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/v1/videos/{videoId}/price")
    fun getPrice(
        @PathVariable("videoId") videoId: String?,
        @RequestParam("userId") userId: String?
    ): ResponseEntity<PriceResource> {
        val video = searchVideo.byId(videoId, getCurrentUser())
        val price = getVideoPrice(video, userId)?.let { PriceResource(amount = it.amount, currency = it.currency) }

        return ResponseEntity(price, HttpStatus.OK)
    }

    @PostMapping("/v1/videos/categories", consumes = ["multipart/form-data"])
    fun tagVideos(
        @RequestParam("file") file: MultipartFile?
    ): ResponseEntity<SuccessResponse> {
        val user = getCurrentUser()

        when (val validationResult = videoTaggingCsvFileValidator.validate(file)) {
            is CsvValidated -> {
                tagVideosCsv(validationResult.entries, user)
                return ResponseEntity(SuccessResponse("Data has been successfully imported!"), HttpStatus.OK)
            }

            is CsvValidatedWithEmptyIds -> {
                tagVideosCsv(validationResult.entriesWithIds, user)

                val rowsWithoutIds = validationResult.entriesWithoutIds.map { it.index }.joinToString()

                return ResponseEntity(
                    SuccessResponse(
                        "Rows $rowsWithoutIds have not been applied because of a missing video ID"
                    ),
                    HttpStatus.OK
                )
            }

            is CsvValidationError -> throw InvalidVideoTaggingCsvFile(validationResult.getMessage())
        }
    }
}
