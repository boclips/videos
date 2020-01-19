package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.domain.model.subject.Subject

class SubjectConverter {
    companion object {
        fun from(subject: Subject) = SubjectResource(
            id = subject.id.value,
            name = subject.name
        )
    }
}
