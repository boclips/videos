package com.boclips.videos.service.infrastructure.analytics

enum class EventType {
    SEARCH,
    ADD_TO_COLLECTION,
    REMOVE_FROM_COLLECTION,
    PLAYBACK,
    RENAME_COLLECTION,
    CHANGE_VISIBILITY,
    CHANGE_COLLECTION_AGE_RANGE,
    BOOKMARK,
    UNBOOKMARK,
    REPLACE_COLLECTION_SUBJECTS
}