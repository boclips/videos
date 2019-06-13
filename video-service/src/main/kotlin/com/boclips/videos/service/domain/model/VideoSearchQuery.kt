package com.boclips.videos.service.domain.model

import com.boclips.search.service.domain.model.Sort
import com.boclips.search.service.domain.model.SortOrder
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.SortKey.RELEASE_DATE
import java.time.Duration
import java.time.LocalDate

enum class SortKey {
    RELEASE_DATE
}

class VideoSearchQuery(
    val text: String,
    val sortBy: SortKey? = null,
    val includeTags: List<String>,
    val excludeTags: List<String>,
    val minDuration: Duration? = null,
    val maxDuration: Duration? = null,
    val releaseDateFrom: LocalDate? = null,
    val releaseDateTo: LocalDate? = null,
    val pageSize: Int,
    val pageIndex: Int,
    val source: SourceType? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null
) {
    fun toSearchQuery(): VideoQuery {
        val sort = sortBy?.let {
            when (it) {
                RELEASE_DATE -> Sort(
                    order = SortOrder.DESC,
                    fieldName = VideoMetadata::releaseDate
                )
            }
        }
        return parseIdsOrPhrase(this.text).let {
            VideoQuery(
                ids = it.ids,
                phrase = it.phrase,
                includeTags = includeTags,
                excludeTags = excludeTags,
                minDuration = minDuration,
                maxDuration = maxDuration,
                source = source,
                sort = sort,
                releaseDateFrom = releaseDateFrom,
                releaseDateTo = releaseDateTo,
                ageRangeMin = ageRangeMin,
                ageRangeMax = ageRangeMax
            )
        }
    }

    override fun toString(): String {
        return "VideoQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize, Sort: $sortBy"
    }

    private fun parseIdsOrPhrase(query: String): VideoQuery {
        val idQueryRegex = "id:(\\S+)".toRegex()
        val match = idQueryRegex.matchEntire(query)
        if (match != null) {
            val ids = match.groupValues[1].split(',')
            return VideoQuery(ids = ids)
        }
        return VideoQuery(phrase = query)
    }
}
