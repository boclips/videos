package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.contentpartner.service.domain.service.contract.ContractService
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerDomainContext")
class DomainContext {

    @Bean
    fun eventConverter(): EventConverter {
        return EventConverter()
    }

    @Bean
    fun contractService(
        contractRepository: ContractRepository
    ): ContractService {
        return ContractService(
            contractRepository
        )
    }

    @Bean
    fun channelService(
        channelRepository: ChannelRepository,
        channelIndex: ChannelIndex
    ): ChannelService {
        return ChannelService(
            channelRepository,
            channelIndex
        )
    }
}
