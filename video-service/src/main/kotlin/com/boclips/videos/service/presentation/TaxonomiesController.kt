package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/taxonomies")
class TaxonomiesController(
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService = accessRuleService, getUserIdOverride = getUserIdOverride) {

    @GetMapping
    fun taxonomies(): ResponseEntity<Any> {
        return ResponseEntity(HttpStatus.OK)
    }
}
