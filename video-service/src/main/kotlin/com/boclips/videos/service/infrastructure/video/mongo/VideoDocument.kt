package com.boclips.videos.service.infrastructure.video.mongo

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.Date

data class VideoDocument(
    @BsonId val id: ObjectId,
    val title: String,
    val description: String,
    val source: Source,
    val playback: Playback,
    val legacy: Legacy,
    val keywords: List<String>,
    val subjects: List<String>,
    val releaseDate: Date,
    val durationSeconds: Int,
    val legalRestrictions: String,
    val language: String?,
    val transcript: String?,
    val topics: List<TopicDocument>?,
    val searchable: Boolean
) {
    data class Source(val contentPartner: ContentPartner, val videoReference: String) {
        data class ContentPartner(val name: String)
    }
    data class Playback(val id: String, val type: String)
    data class Legacy(val type: String)
}