package com.boclips.videos.service.config.security

object UserRoles {
    const val E2E: String = "E2E"
    const val CREATE_SUBJECT = "CREATE_SUBJECT"

    const val VIEW_EVENTS = "VIEW_EVENTS"
    const val INSERT_EVENTS = "INSERT_EVENTS"

    const val REMOVE_VIDEOS = "REMOVE_VIDEOS"
    const val VIEW_VIDEOS = "VIEW_VIDEOS"
    const val DOWNLOAD_VIDEOS = "DOWNLOAD_VIDEOS"
    const val VIEW_DISABLED_VIDEOS = "VIEW_DISABLED_VIDEOS"
    const val INSERT_VIDEOS = "INSERT_VIDEOS"
    const val UPDATE_VIDEOS = "UPDATE_VIDEOS"

    const val INSERT_COLLECTIONS = "INSERT_COLLECTIONS"
    const val VIEW_COLLECTIONS = "VIEW_COLLECTIONS"
    const val UPDATE_COLLECTIONS = "UPDATE_COLLECTIONS"
    const val DELETE_COLLECTIONS = "DELETE_COLLECTIONS"

    const val REBUILD_SEARCH_INDEX = "REBUILD_SEARCH_INDEX"
}