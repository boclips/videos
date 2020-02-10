package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.application.UpdateContentPartner
import com.boclips.videos.api.request.contentpartner.ContentPartnerFilterRequest
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.ContentPartnerWrapperResource
import com.boclips.videos.api.response.contentpartner.ContentPartnersResource
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.projections.WithProjection
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/content-partners")
class ContentPartnerController(
    private val videoRepository: VideoRepository,
    private val createContentPartner: CreateContentPartner,
    private val updateContentPartner: UpdateContentPartner,
    private val fetchContentPartner: GetContentPartner,
    private val fetchContentPartners: GetContentPartners,
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    private val contentPartnerToResourceConverter: ContentPartnerToResourceConverter,
    private val withProjection: WithProjection
) : BaseController() {
    @PostMapping("/{contentPartnerId}/videos/search")
    fun postSearchVideoByProviderId(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @RequestBody contentPartnerVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromContentPartnerId(
            ContentPartnerId(value = contentPartnerId),
            contentPartnerVideoId
        )

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }

    @GetMapping
    fun getContentPartners(contentPartnerFilterRequest: ContentPartnerFilterRequest): ContentPartnersResource {
        val user = getCurrentUser()
        val contentPartners = fetchContentPartners(
            name = contentPartnerFilterRequest.name,
            official = contentPartnerFilterRequest.official,
            accreditedToYtChannelId = contentPartnerFilterRequest.accreditedToYtChannelId
        )

        val resources = contentPartners.map {
            contentPartnerToResourceConverter.convert(it, user)
        }

        return ContentPartnersResource(_embedded = ContentPartnerWrapperResource(contentPartners = resources))
    }

    @GetMapping("/{id}")
    fun getContentPartner(@PathVariable("id") @NotBlank contentPartnerId: String?): ResponseEntity<MappingJacksonValue> {
        val user = getCurrentUser()
        val contentPartnerResource = fetchContentPartner(contentPartnerId!!, user)
            .copy(_links = listOf(contentPartnersLinkBuilder.self(contentPartnerId)).map { it.rel to it }.toMap())

        val body: MappingJacksonValue = withProjection(
            contentPartnerResource
        )

        return ResponseEntity(body, HttpStatus.OK)
    }

    @PostMapping
    fun postContentPartner(@Valid @RequestBody createUpsertContentPartnerRequest: UpsertContentPartnerRequest): ResponseEntity<Void> {
        val contentPartner = createContentPartner(createUpsertContentPartnerRequest)

        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                contentPartnersLinkBuilder.self(contentPartner.contentPartnerId.value).href
            )
        }, HttpStatus.CREATED)
    }

    @PatchMapping("/{id}")
    fun patchContentPartner(
        @PathVariable("id") contentPartnerId: String,
        @Valid @RequestBody updateUpsertContentPartnerRequest: UpsertContentPartnerRequest
    ): ResponseEntity<Void> {
        updateContentPartner(contentPartnerId = contentPartnerId, upsertRequest = updateUpsertContentPartnerRequest)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
