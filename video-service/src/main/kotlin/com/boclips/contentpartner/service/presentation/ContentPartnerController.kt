package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.application.UpdateContentPartner
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.videos.api.request.contentpartner.ContentPartnerFilterRequest
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.contentpartner.ContentPartnerWrapperResource
import com.boclips.videos.api.response.contentpartner.ContentPartnersResource
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.VideoRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    private val signedLinkProvider: SignedLinkProvider
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
        val contentPartners = fetchContentPartners(
            name = contentPartnerFilterRequest.name,
            official = contentPartnerFilterRequest.official,
            accreditedToYtChannelId = contentPartnerFilterRequest.accreditedToYtChannelId
        )

        val resources = contentPartners.map {
            contentPartnerToResourceConverter.convert(it)
        }

        return ContentPartnersResource(_embedded = ContentPartnerWrapperResource(contentPartners = resources))
    }

    @GetMapping("/{id}")
    fun getContentPartner(@PathVariable("id") @NotBlank contentPartnerId: String?): ResponseEntity<ContentPartnerResource> {
        val contentPartnerResource = fetchContentPartner(contentPartnerId!!)
            .let { contentPartnerToResourceConverter.convert(it) }
            .copy(_links = listOf(contentPartnersLinkBuilder.self(contentPartnerId)).map { it.rel to it }.toMap())

        return ResponseEntity(contentPartnerResource, HttpStatus.OK)
    }

    @PostMapping
    fun postContentPartner(@Valid @RequestBody upsertContentPartnerRequest: UpsertContentPartnerRequest): ResponseEntity<Void> {
        val contentPartner = createContentPartner(upsertContentPartnerRequest)

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

    @PostMapping("/signed-upload-link")
    fun signedUploadLink(
        @RequestBody signedLinkRequest: SignedLinkRequest
    ): ResponseEntity<Void> {
        val link = signedLinkProvider.getLink(signedLinkRequest.filename)
        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                link.toString()
            )
        }, HttpStatus.NO_CONTENT)
    }
}
