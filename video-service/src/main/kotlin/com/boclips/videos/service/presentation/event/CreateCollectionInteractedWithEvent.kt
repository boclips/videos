package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.application.analytics.InvalidEventException

data class CreateCollectionInteractedWithEvent (val subtype: String) : EventCommand() {
    override fun isValidOrThrows(){
        if(this.subtype.isNullOrBlank())
            throw InvalidEventException("subtype must be specified")
    }
}
