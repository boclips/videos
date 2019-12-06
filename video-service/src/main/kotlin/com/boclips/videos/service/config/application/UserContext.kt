package com.boclips.videos.service.config.application

import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.common.UserId
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class UserContext(val request: HttpServletRequest) {
    fun getUser(): UserId {
        return getCurrentUserId()
    }
}