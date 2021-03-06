package com.boclips.videos.service.presentation.projections

import com.boclips.security.utils.UserExtractor.currentUserHasAnyRole
import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PricingProjection
import com.boclips.videos.api.PublicApiProjection
import com.boclips.videos.api.ResourceProjection
import com.boclips.videos.service.config.security.UserRoles.BOCLIPS_SERVICE
import com.boclips.videos.service.config.security.UserRoles.BOCLIPS_WEB_APP
import com.boclips.videos.service.config.security.UserRoles.E2E
import com.boclips.videos.service.config.security.UserRoles.HQ
import com.boclips.videos.service.config.security.UserRoles.LEGACY_PUBLISHER

interface ProjectionResolver {
    fun resolveProjection(): Class<out ResourceProjection>
}

class RoleBasedProjectionResolver : ProjectionResolver {
    override fun resolveProjection(): Class<out ResourceProjection> {
        return when {
            currentUserHasAnyRole(LEGACY_PUBLISHER, HQ, BOCLIPS_SERVICE, E2E) -> BoclipsInternalProjection::class.java
            currentUserHasAnyRole(BOCLIPS_WEB_APP) -> PricingProjection::class.java
            else -> PublicApiProjection::class.java
        }
    }
}
