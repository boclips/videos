package com.boclips.contentpartner.service.presentation

import com.boclips.videos.api.response.contentpartner.ContentCategoriesResource
import com.boclips.videos.api.response.contentpartner.ContentCategoriesWrapperResource
import com.boclips.videos.api.response.contentpartner.ContentCategoryResource
import com.boclips.videos.service.domain.model.video.ContentCategories
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.presentation.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/content-categories")
class ContentCategoriesController(
    accessRuleService: AccessRuleService,
    getUserIdOverride: GetUserIdOverride
) : BaseController(accessRuleService, getUserIdOverride) {
    @GetMapping
    fun contentCategories(): ContentCategoriesResource {
        return ContentCategoriesResource(
            _embedded = ContentCategoriesWrapperResource(
                contentCategories = ContentCategories.values().map {
                    ContentCategoryResource(
                        key = it.name,
                        label = it.value
                    )
                }.toList()
            )
        )
    }
}
