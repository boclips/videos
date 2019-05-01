package com.boclips.videos.service.infrastructure.analytics

enum class EventType {
    SEARCH,
    ADD_TO_COLLECTION,
    REMOVE_FROM_COLLECTION,
    PLAYBACK,
    RENAME_COLLECTION,
    CHANGE_VISIBILITY,
    CHANGE_AGE_RANGE,
    BOOKMARK,
    UNBOOKMARK,
    UPDATE_SUBJECTS,
    UPDATE_AGE_RANGE,
    REPLACE_SUBJECTS
}