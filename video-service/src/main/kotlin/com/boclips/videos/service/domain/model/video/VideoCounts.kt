package com.boclips.videos.service.domain.model.video

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.videos.service.domain.model.subject.SubjectId

class VideoCounts(
    val total: Long,
    val subjects: List<SubjectFacet>,
    val ageRanges: List<AgeRangeFacet>,
    val durations: List<DurationFacet>
)

class SubjectFacet(val subjectId: SubjectId, val total: Long)
class AgeRangeFacet(val ageRangeId: AgeRangeId, val total: Long)
class DurationFacet(val durationId: String, val total: Long)
