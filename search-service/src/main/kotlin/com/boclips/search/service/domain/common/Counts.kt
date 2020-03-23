package com.boclips.search.service.domain.common

class Counts(val hits: Long, val buckets: List<FilterCounts>? = null) {
    fun getCounts(bucket: Bucket): List<Count> {
        return when (bucket) {
            is Bucket.SubjectsBucket -> {
                buckets?.find { it.key is Bucket.SubjectsBucket }?.counts ?: emptyList()
            }
        }
    }
}

class FilterCounts(val key: Bucket, val counts: List<Count>)

data class Count(val id: String, val hits: Long)

sealed class Bucket {
    object SubjectsBucket : Bucket()
}
