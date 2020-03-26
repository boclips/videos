package com.boclips.videos.service.domain.service.user

import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.user.User

interface AccessRuleService {
    fun getRules(user: User): AccessRules
}
