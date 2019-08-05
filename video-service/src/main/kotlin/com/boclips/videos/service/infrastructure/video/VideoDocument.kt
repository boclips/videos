package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.Date

data class VideoDocument(
    @BsonId val id: ObjectId,
    val title: String,
    val description: String,
    val source: SourceDocument,
    val playback: PlaybackDocument?,
    val legacy: LegacyDocument,
    val keywords: List<String>,
    val subjects: List<SubjectDocument>,
    val releaseDate: Date,
    val legalRestrictions: String,
    val language: String?,
    val transcript: String?,
    val topics: List<TopicDocument>?,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val rating: List<UserRatingDocument> = emptyList(),
    val distributionMethods: Set<DistributionMethodDocument>?,
    val tags: List<UserTagDocument> = emptyList()
)
