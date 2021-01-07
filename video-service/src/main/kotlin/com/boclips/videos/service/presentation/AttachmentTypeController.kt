package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.attachment.AttachmentTypesResource
import com.boclips.videos.service.application.attachment.GetAttachmentTypes
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.infrastructure.user.GetUserOrganisationAndExternalId
import com.boclips.videos.service.presentation.converters.AttachmentTypeToResourceConverter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/attachment-types")
class AttachmentTypeController(
        private val getAttachmentTypes: GetAttachmentTypes,
        private val attachmentTypeConverter: AttachmentTypeToResourceConverter,
        getUserOrganisationAndExternalId: GetUserOrganisationAndExternalId,
        accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserOrganisationAndExternalId) {

    @GetMapping
    fun attachmentTypes(): ResponseEntity<AttachmentTypesResource> {
        val attachmentTypes = getAttachmentTypes()

        return ResponseEntity(attachmentTypeConverter.convert(attachmentTypes), HttpStatus.OK)
    }
}
