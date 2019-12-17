package com.boclips.contentpartner.service.domain.model

enum class DistributionMethod {
    DOWNLOAD,
    STREAM;

    companion object {
        val ALL = setOf(
            DOWNLOAD,
            STREAM
        )
    }
}
