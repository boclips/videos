package com.boclips.contentpartner.service.domain.model.contentpartner

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
