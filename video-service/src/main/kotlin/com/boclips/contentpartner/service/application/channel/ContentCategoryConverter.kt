package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.ContentCategory
import com.boclips.videos.api.request.channel.ContentCategoryRequest
import com.boclips.videos.api.response.channel.ContentCategoryResource

class ContentCategoryConverter {

    companion object {
        fun convert(categoryRequest: ContentCategoryRequest): ContentCategory {
            return when (categoryRequest) {
                ContentCategoryRequest.VIRTUAL_REALITY_360 -> ContentCategory.VIRTUAL_REALITY_360
                ContentCategoryRequest.ANIMATION -> ContentCategory.ANIMATION
                ContentCategoryRequest.DOCUMENTARY_SHORTS -> ContentCategory.DOCUMENTARY_SHORTS
                ContentCategoryRequest.EARLY_CHILDHOOD -> ContentCategory.EARLY_CHILDHOOD
                ContentCategoryRequest.EDUCATIONAL_SONGS -> ContentCategory.EDUCATIONAL_SONGS
                ContentCategoryRequest.INSPIRATION_FOR_LESSONS -> ContentCategory.INSPIRATION_FOR_LESSONS
                ContentCategoryRequest.INSTRUCTIONAL_VIDEOS -> ContentCategory.INSTRUCTIONAL_VIDEOS
                ContentCategoryRequest.INTERVIEW -> ContentCategory.INTERVIEW
                ContentCategoryRequest.HISTORICAL_ARCHIVE -> ContentCategory.HISTORICAL_ARCHIVE
                ContentCategoryRequest.MUSIC -> ContentCategory.MUSIC
                ContentCategoryRequest.NARRATED -> ContentCategory.NARRATED
                ContentCategoryRequest.NEWS_STORIES -> ContentCategory.NEWS_STORIES
                ContentCategoryRequest.PRACTICAL_EXPERIMENTS -> ContentCategory.PRACTICAL_EXPERIMENTS
                ContentCategoryRequest.SONGS -> ContentCategory.SONGS
                ContentCategoryRequest.STOCK_CLIPS -> ContentCategory.STOCK_CLIPS
                ContentCategoryRequest.STUDY_SKILLS -> ContentCategory.STUDY_SKILLS
                ContentCategoryRequest.SUSTAINABILITY -> ContentCategory.SUSTAINABILITY
                ContentCategoryRequest.WITH_A_CHILD_HOST -> ContentCategory.WITH_A_CHILD_HOST
                ContentCategoryRequest.WITH_A_HOST -> ContentCategory.WITH_A_HOST
            }
        }

        fun convert(contentCategories: List<ContentCategoryRequest>) =
            contentCategories.map { convert(it) }

        fun convertToResource(contentCategory: ContentCategory): ContentCategoryResource {
            return ContentCategoryResource(key = contentCategory.name, label = contentCategory.value)
        }

        fun convertToResource(categories: List<ContentCategory>): List<ContentCategoryResource> = categories.map { convertToResource(it) }
    }
}
