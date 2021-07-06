package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.*

data class VideoDocument(
    @BsonId val id: ObjectId,
    val title: String,
    val description: String,
    val additionalDescription: String? = null,
    val source: SourceDocument,
    val playback: PlaybackDocument?,
    val contentTypes: List<String> = emptyList(),
    val keywords: List<String>,
    val subjects: List<SubjectDocument>,
    val attachments: List<AttachmentDocument> = emptyList(),
    val releaseDate: Date,
    val ingestedAt: String?,
    val updatedAt: String?,
    val legalRestrictions: String,
    val language: String?,
    val transcript: String?,
    val isTranscriptHumanGenerated: Boolean?,
    val isTranscriptRequested: Boolean?,
    val isVoiced: Boolean?,
    val topics: List<TopicDocument>?,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val ageRangeSetManually: Boolean? = null,
    val rating: List<UserRatingDocument> = emptyList(),
    val tags: List<UserTagDocument> = emptyList(),
    val promoted: Boolean? = null,
    val subjectsWereSetManually: Boolean? = false,
    val contentWarnings: List<ContentWarningDocument>? = emptyList(),
    val deactivated: Boolean? = false,
    val activeVideoId: String? = null,
    val categories: VideoCategoriesDocument? = null,
    val analysisFailed: Boolean? = false,
)
