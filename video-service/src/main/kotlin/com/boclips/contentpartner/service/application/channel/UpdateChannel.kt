package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.ChannelNotFoundException
import com.boclips.contentpartner.service.application.exceptions.MissingContractException
import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.SingleChannelUpdate
import com.boclips.contentpartner.service.domain.model.channel.UpdateChannelResult
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.request.channel.LegalRestrictionsRequest
import org.springframework.stereotype.Component

@Component
class UpdateChannel(
    private val channelService: ChannelService,
    private val channelUpdatesConverter: ChannelUpdatesConverter,
    private val createLegalRestrictions: CreateLegalRestrictions
) {
    operator fun invoke(channelId: String, upsertRequest: ChannelRequest): Channel {
        val id = ChannelId(value = channelId)

        upsertRequest.legalRestrictions?.let { legalRestrictionsRequest ->
            if (legalRestrictionsRequest.id.isNullOrEmpty()) {
                val legalRestrictions = createLegalRestrictions(legalRestrictionsRequest.text)
                upsertRequest.legalRestrictions =
                    LegalRestrictionsRequest(id = legalRestrictions.id.value)
            }
        }

        val updateCommands = channelUpdatesConverter.convert(id, upsertRequest)
        val updateChannelResult = channelService.update(SingleChannelUpdate(id = id, updateCommands = updateCommands))

        return when (updateChannelResult) {
            is UpdateChannelResult.Success -> updateChannelResult.channel
            is UpdateChannelResult.ChannelNotFound -> throw ChannelNotFoundException(id)
            is UpdateChannelResult.MissingContract -> throw MissingContractException()
        }
    }
}
