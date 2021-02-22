package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.service.user.UserService
import mu.KLogging

class GetVideoPrice(
    private val userService: UserService,
    private val priceComputingService: PriceComputingService
) {

    companion object : KLogging()

    operator fun invoke(video: BaseVideo, userId: String?): Price? {
        val videoPrices = userId?.let { userService.getOrganisationOfUser(it)?.deal?.prices }

        val price =  priceComputingService.computeVideoPrice(
            videoId = video.videoId,
            organisationPrices = videoPrices,
            channel = video.channel.channelId,
            playback = video.playback,
            videoTypes = video.types
        )
        logger.info { "calculated price of ${video.videoId.value} - ${price?.amount}" }
        return price
    }
}
