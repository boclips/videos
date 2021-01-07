package com.boclips.videos.service.domain.model.user

data class Organisation(
    val organisationId: OrganisationId,
    val allowOverridingUserIds: Boolean,
    val deal: Deal
)
