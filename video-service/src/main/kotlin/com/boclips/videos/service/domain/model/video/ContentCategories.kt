package com.boclips.videos.service.domain.model.video

import com.boclips.videos.api.response.contentpartner.ContentCategoryResource

enum class ContentCategories(val value: String) {
    VIRTUAL_REALITY_360("360 and Virtual reality"),
    ANIMATION("Animation"),
    DOCUMENTARY_SHORTS("Documentary shorts"),
    EARLY_CHILDHOOD("Early childhood"),
    EDUCATIONAL_SONGS("Educational songs"),
    INSPIRATION_FOR_LESSONS("Inspiration for lessons"),
    INSTRUCTIONAL_VIDEOS("Instructional videos"),
    INTERVIEW("Interviews"),
    HISTORICAL_ARCHIVE("Historical archive"),
    MUSIC("Music"),
    NARRATED("Narrated"),
    NEWS_STORIES("News stories"),
    PRACTICAL_EXPERIMENTS("Practical experiments"),
    SONGS("Songs"),
    STOCK_CLIPS("Stock clips"),
    STUDY_SKILLS("Study skills"),
    SUSTAINABILITY("Sustainability"),
    WITH_A_CHILD_HOST("With a child host"),
    WITH_A_HOST("With a host")
}

fun toContentCategoryResource(key: String): ContentCategoryResource {
    val category = ContentCategories.values().find { it.name == key }
        ?: throw IllegalStateException("Invalid content category")

    return ContentCategoryResource(key = category.name, label = category.value)
}