package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.tag.CreateTagRequest
import com.boclips.videos.api.response.tag.TagResource
import com.boclips.videos.api.response.tag.TagsResource
import com.boclips.videos.service.application.exceptions.TagExistsException
import com.boclips.videos.service.application.tag.CreateTag
import com.boclips.videos.service.application.tag.DeleteTag
import com.boclips.videos.service.application.tag.GetTag
import com.boclips.videos.service.application.tag.GetTags
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.converters.TagConverter
import com.boclips.videos.service.presentation.hateoas.TagsLinkBuilder
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/tags")
class TagController(
    private val getTag: GetTag,
    private val deleteTag: DeleteTag,
    private val getTags: GetTags,
    private val createTag: CreateTag,
    private val tagsConverter: TagConverter,
    private val tagsLinkBuilder: TagsLinkBuilder,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService,
    userService: UserService
) : BaseController(accessRuleService, getUserIdOverride, userService) {

    @GetMapping("/{id}")
    fun tag(@PathVariable id: String): ResponseEntity<TagResource> {
        val tag = getTag(id)

        val resource = tagsConverter.convert(tag)

        return ResponseEntity(resource, HttpStatus.OK)
    }

    @GetMapping
    fun tags(): ResponseEntity<TagsResource> {
        val allTags = getTags()

        val resource = tagsConverter.convert(allTags)

        return ResponseEntity(resource, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun removeTags(@PathVariable id: String): ResponseEntity<Unit> {
        deleteTag(TagId(value = id))
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping
    fun createATag(@Valid @RequestBody createTagRequest: CreateTagRequest): ResponseEntity<Any> {
        val tag = try {
            createTag(createTagRequest)
        } catch (e: TagExistsException) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    error = "Error creating tag",
                    message = "The tag ${createTagRequest.label} already exists",
                    status = HttpStatus.CONFLICT
                )
            )
        }

        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, tagsLinkBuilder.tag(id = tag.id)?.href)
        }

        return ResponseEntity(headers, HttpStatus.CREATED)
    }
}
