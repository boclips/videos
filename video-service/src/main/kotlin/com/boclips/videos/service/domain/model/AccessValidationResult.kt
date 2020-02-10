package com.boclips.videos.service.domain.model

data class AccessValidationResult(val successful: Boolean = false, val error: AccessError?) {
    companion object {
        val SUCCESS = AccessValidationResult(
            successful = true,
            error = null
        )
    }
}
