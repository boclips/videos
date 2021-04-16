package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.attachment.AttachmentTypesResource
import com.boclips.videos.service.application.attachment.GetAttachmentTypes
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
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
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService,
    userService: UserService
) : BaseController(accessRuleService, getUserIdOverride, userService) {

    @GetMapping
    fun attachmentTypes(): ResponseEntity<AttachmentTypesResource> {
        val attachmentTypes = getAttachmentTypes()

        return ResponseEntity(attachmentTypeConverter.convert(attachmentTypes), HttpStatus.OK)
    }
}
