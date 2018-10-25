package com.boclips.videos.service.presentation.event

abstract class EventCommand {
    abstract fun isValidOrThrows()
}
