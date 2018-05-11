package com.boclips.api.contentproviders

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

@Document(collection = "sources")
data class ContentProvider(
        val name: String,
        val uuid: String = UUID.randomUUID().toString(),
        @Field("date_created") val dateCreated: String? = null,
        @Field("date_updated") val dateUpdated: String? = null,
        @Id var id: String? = null
)