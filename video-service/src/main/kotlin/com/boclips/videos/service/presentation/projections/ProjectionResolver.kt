package com.boclips.videos.service.presentation.projections

import com.boclips.videos.service.application.currentUserHasAnyRole
import com.boclips.videos.service.application.currentUserHasRole
import com.boclips.videos.service.config.security.UserRoles.BACKOFFICE
import com.boclips.videos.service.config.security.UserRoles.PUBLISHER
import com.boclips.videos.service.config.security.UserRoles.TEACHER

interface ProjectionResolver {
    fun resolveProjection(): Class<out ResourceProjection>
}

class RoleBasedProjectionResolver : ProjectionResolver {
    override fun resolveProjection(): Class<out ResourceProjection> {
        return when {
            currentUserHasAnyRole(PUBLISHER, BACKOFFICE) -> BoclipsInternalProjection::class.java
            currentUserHasRole(TEACHER) -> TeachersProjection::class.java
            else -> PublicApiProjection::class.java
        }
    }
}

