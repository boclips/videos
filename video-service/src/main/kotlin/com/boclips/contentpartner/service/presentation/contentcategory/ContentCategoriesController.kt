package com.boclips.contentpartner.service.presentation.contentcategory

import com.boclips.videos.api.response.channel.ContentCategoriesResource
import com.boclips.videos.api.response.channel.ContentCategoriesWrapperResource
import com.boclips.videos.api.response.channel.ContentCategoryResource
import com.boclips.contentpartner.service.domain.model.channel.ContentCategory
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/content-categories")
class ContentCategoriesController(
    accessRuleService: AccessRuleService,
    getUserIdOverride: GetUserIdOverride,
    userService: UserService
) : BaseController(accessRuleService, getUserIdOverride, userService) {
    @GetMapping
    fun contentCategories(): ContentCategoriesResource {
        return ContentCategoriesResource(
            _embedded = ContentCategoriesWrapperResource(
                contentCategories = ContentCategory.values().map {
                    ContentCategoryResource(
                        key = it.name,
                        label = it.value
                    )
                }.toList()
            )
        )
    }
}
