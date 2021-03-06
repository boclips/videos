package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.contentwarning.CreateContentWarningRequest
import com.boclips.videos.api.response.contentwarning.ContentWarningResource
import com.boclips.videos.api.response.contentwarning.ContentWarningsResource
import com.boclips.videos.service.application.contentwarning.CreateContentWarning
import com.boclips.videos.service.application.contentwarning.GetAllContentWarnings
import com.boclips.videos.service.application.contentwarning.GetContentWarning
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.converters.ContentWarningToResourceConverter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/content-warnings")
class ContentWarningController(
    private val getContentWarning: GetContentWarning,
    private val getAllContentWarnings: GetAllContentWarnings,
    private val createContentWarning: CreateContentWarning,
    private val contentWarningToResourceConverter: ContentWarningToResourceConverter,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService,
    userService: UserService
) : BaseController(accessRuleService, getUserIdOverride, userService) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<ContentWarningResource> {
        val resource = getContentWarning(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(contentWarningToResourceConverter.convert(resource), HttpStatus.OK)
    }

    @GetMapping
    fun getAll(): ResponseEntity<ContentWarningsResource> {
        val contentWarnings = getAllContentWarnings()

        return ResponseEntity(contentWarningToResourceConverter.convert(contentWarnings), HttpStatus.OK)
    }

    @PostMapping
    fun create(@Valid @RequestBody request: CreateContentWarningRequest?): ResponseEntity<ContentWarningResource> {
        val contentWarning = createContentWarning(request!!)

        return ResponseEntity(contentWarningToResourceConverter.convert(contentWarning), HttpStatus.CREATED)
    }
}
