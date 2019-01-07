package com.boclips.search.service.domain

import kotlin.reflect.KProperty1

data class Filter private constructor(val field: String, val value: Any) {
    companion object {
        operator fun <T : Any> invoke(field: KProperty1<VideoMetadata, T>, value: T) = Filter(field.name, value)
    }
}