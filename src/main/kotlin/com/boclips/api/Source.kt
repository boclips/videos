package com.boclips.api

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "sources")
data class Source(
        val name: String,
        val uuid: String,
        @Field("date_created") val dateCreated: String,
        @Field("date_updated") val dateUpdated: String
) {
    @Id
    lateinit var id: String
}