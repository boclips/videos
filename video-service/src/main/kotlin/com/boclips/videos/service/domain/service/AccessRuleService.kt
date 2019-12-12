package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.User

interface AccessRuleService {
    fun getRules(user: User): AccessRules
}
