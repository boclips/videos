package com.boclips.videos.service.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "sources")
data class ContentProvider(
        val name: String,
        @Id var id: String? = null
)