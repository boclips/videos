package com.boclips.videos.infrastructure.contentprovider

import com.boclips.videos.domain.model.ContentProvider
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*
import javax.persistence.Id

@Document(collection = "sources")
data class ContentProviderEntity(
        @Id val id: String? = null,
        val name: String,
        val uuid: String = UUID.randomUUID().toString(),
        @Field("date_created") val dateCreated: String? = null,
        @Field("date_updated") val dateUpdated: String? = null) {
    fun toContentProvider(): ContentProvider {
        return ContentProvider(id = id, name = name)
    }
}