package com.boclips.videos.service.domain.model.video

import com.boclips.contentpartner.service.domain.model.AgeRangeId
import com.boclips.videos.service.domain.model.subject.SubjectId

class VideoCounts(val total: Long, val subjects: List<SubjectFacet>, val ageRanges: List<AgeRangeFacet>)

class SubjectFacet(val subjectId: SubjectId, val total: Long)
class AgeRangeFacet(val ageRangeId: AgeRangeId, val total: Long)