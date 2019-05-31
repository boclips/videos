package com.boclips.search.service.domain.model

import kotlin.reflect.KProperty1

data class Sort<METADATA>(
    val fieldName: KProperty1<METADATA, Comparable<*>>,
    val order: SortOrder
)