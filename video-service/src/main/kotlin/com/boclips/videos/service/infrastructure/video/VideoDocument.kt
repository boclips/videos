package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
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
    val contentType: String? = null,
    val keywords: List<String>,
    val subjects: List<SubjectDocument>,
    val attachments: List<AttachmentDocument> = emptyList(),
    val releaseDate: Date,
    val ingestedAt: String?,
    val legalRestrictions: String,
    val language: String?,
    val transcript: String?,
    val topics: List<TopicDocument>?,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val ageRangeSetManually: Boolean? = null,
    val rating: List<UserRatingDocument> = emptyList(),
    val tags: List<UserTagDocument> = emptyList(),
    val promoted: Boolean? = null,
    val subjectsWereSetManually: Boolean? = false,
    val contentWarnings: List<ContentWarningDocument>? = emptyList()
)

