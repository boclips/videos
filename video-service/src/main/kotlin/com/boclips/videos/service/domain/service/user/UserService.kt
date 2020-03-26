package com.boclips.videos.service.domain.service.user

import com.boclips.videos.service.domain.model.user.Organisation

interface UserService {
    fun getSubjectIds(userId: String): Set<String>?
    fun getOrganisationOfUser(userId: String): Organisation?
    fun isShareCodeValid(referrerId: String, shareCode: String): Boolean
}
