package com.boclips.search.service.domain.common.model

import kotlin.reflect.KProperty1

sealed class Sort<METADATA> {
    data class ByField<METADATA>(
        val fieldName: KProperty1<METADATA, Comparable<*>?>,
        val order: SortOrder
    ) : Sort<METADATA>()

    class ByRandom<METADATA> : Sort<METADATA>()
}
