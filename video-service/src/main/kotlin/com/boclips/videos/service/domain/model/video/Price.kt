package com.boclips.videos.service.domain.model.video

import java.math.BigDecimal
import java.util.Currency

data class Price(val amount: BigDecimal, val currency: Currency = Currency.getInstance("USD")) {
    companion object {
        fun getDefault(videoTypes: List<VideoType>): Price {
            return videoTypes.map {
                when (it) {
                    VideoType.INSTRUCTIONAL_CLIPS -> 600
                    VideoType.NEWS -> 300
                    VideoType.STOCK -> 150
                }
            }
                .maxOrNull()
                .let { Price(amount = BigDecimal(it ?: 600)) }
        }
    }
}
