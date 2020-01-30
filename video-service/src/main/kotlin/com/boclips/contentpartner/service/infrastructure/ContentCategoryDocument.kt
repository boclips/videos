package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.ContentCategory

data class ContentCategoryDocument(
    val key: String?

) {
    companion object {
        fun from(contentCategory: ContentCategory): ContentCategoryDocument {
            return ContentCategoryDocument(key = contentCategory.key)
        }
    }
}