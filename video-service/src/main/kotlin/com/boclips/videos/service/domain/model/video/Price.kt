package com.boclips.videos.service.domain.model.video

import java.math.BigDecimal
import java.util.Currency

data class Price(val amount: BigDecimal, val currency: Currency = Currency.getInstance("USD")) {
    companion object {
        fun getDefault(videoTypes: List<ContentType>): Price {
            return videoTypes.map {
                when (it) {
                    ContentType.INSTRUCTIONAL_CLIPS -> 600
                    ContentType.NEWS -> 300
                    ContentType.STOCK -> 150
                }
            }
                .maxOrNull()
                .let { Price(amount = BigDecimal(it ?: 600)) }
        }
    }
}
