package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.subject.SubjectId

class VideoCounts(val total: Long, val subjects: List<SubjectFacet>)

class SubjectFacet(val subjectId: SubjectId, val total: Long)