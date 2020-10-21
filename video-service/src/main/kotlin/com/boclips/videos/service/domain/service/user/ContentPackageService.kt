package com.boclips.videos.service.domain.service.user

import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.contentpackage.ContentPackageId

interface ContentPackageService {
    fun getAccessRules(id: ContentPackageId): AccessRules?
}
