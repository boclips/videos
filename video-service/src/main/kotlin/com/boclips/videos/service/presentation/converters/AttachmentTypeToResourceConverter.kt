package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.attachment.AttachmentTypeResource
import com.boclips.videos.api.response.attachment.AttachmentTypesResource
import com.boclips.videos.api.response.attachment.AttachmentTypesWrapperResource
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import org.springframework.stereotype.Component

@Component
class AttachmentTypeToResourceConverter {
    fun convert(attachmentType: AttachmentType): AttachmentTypeResource = AttachmentTypeResource(
        name = attachmentType.name,
        label = attachmentType.label
    )

    fun convert(attachmentTypes: List<AttachmentType>): AttachmentTypesResource {
        return AttachmentTypesResource(
            _embedded = AttachmentTypesWrapperResource(attachmentTypes = attachmentTypes.map { convert(it) })
        )
    }
}
