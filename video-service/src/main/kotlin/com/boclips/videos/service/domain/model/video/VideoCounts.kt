package com.boclips.videos.service.domain.model.video

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.channel.ChannelId

class VideoCounts(
    val total: Long,
    val subjects: List<SubjectFacet>,
    val ageRanges: List<AgeRangeFacet>,
    val durations: List<DurationFacet>,
    val attachmentTypes: List<AttachmentTypeFacet>,
    val channels: List<ChannelFacet>,
    val videoTypes: List<VideoTypeFacet>
)

class SubjectFacet(val subjectId: SubjectId, val total: Long)
class AgeRangeFacet(val ageRangeId: AgeRangeId, val total: Long)
class DurationFacet(val durationId: String, val total: Long)
class AttachmentTypeFacet(val attachmentType: String, val total: Long)
class ChannelFacet(val channelId: ChannelId, val total: Long)
class VideoTypeFacet(val typeId: String, val total: Long)
