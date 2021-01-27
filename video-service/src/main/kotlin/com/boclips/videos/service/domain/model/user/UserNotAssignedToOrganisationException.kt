package com.boclips.videos.service.domain.model.user

class UserNotAssignedToOrganisationException(userId: UserId):
        RuntimeException("No organisation found for user $userId")
