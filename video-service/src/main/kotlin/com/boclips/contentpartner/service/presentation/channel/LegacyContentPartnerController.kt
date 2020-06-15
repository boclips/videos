package com.boclips.contentpartner.service.presentation.channel

import com.boclips.contentpartner.service.application.channel.GetChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.presentation.converters.LegacyContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.LegacyContentPartnerLinkBuilder
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.LegacyContentPartnerWrapperResource
import com.boclips.videos.api.response.channel.LegacyContentPartnersResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotBlank

/*
 * These endpoints are public facing, we need to check with clients before axing them
 */
@RestController
@RequestMapping("/v1/content-partners")
class LegacyContentPartnerController(
    private val fetchChannel: GetChannel,
    private val fetchChannels: GetChannels,
    private val legacyContentPartnerLinkBuilder: LegacyContentPartnerLinkBuilder,
    private val legacyContentPartnerToResourceConverter: LegacyContentPartnerToResourceConverter
) {
    @GetMapping
    @Deprecated("Please use /v1/channels instead")
    fun getLegacyContentPartners(channelFilterRequest: ChannelFilterRequest): LegacyContentPartnersResource {
        val channels = fetchChannels(
            name = channelFilterRequest.name,
            ingestTypes = channelFilterRequest.ingestType
        )

        val resources = channels.map {
            legacyContentPartnerToResourceConverter.convert(it)
        }

        return LegacyContentPartnersResource(_embedded = LegacyContentPartnerWrapperResource(contentPartners = resources))
    }

    @GetMapping("/{id}")
    @Deprecated("Please use /v1/channels/{id} instead")
    fun getLegacyContentPartner(@PathVariable("id") @NotBlank contentPartnerId: String?): ResponseEntity<ChannelResource> {
        val channelResource = fetchChannel(contentPartnerId!!)
            .let { legacyContentPartnerToResourceConverter.convert(it) }
            .copy(_links = listOf(legacyContentPartnerLinkBuilder.self(contentPartnerId)).map { it.rel to it }.toMap())

        return ResponseEntity(channelResource, HttpStatus.OK)
    }
}
