package com.boclips.videos.service.config.security

object UserRoles {
    const val API = "API"
    const val TEACHER = "TEACHER"
    const val PUBLISHER = "PUBLISHER"
    const val BACKOFFICE = "BACKOFFICE"

    const val CREATE_SUBJECT = "CREATE_SUBJECT"
    const val DELETE_SUBJECT = "DELETE_SUBJECT"

    const val VIEW_EVENTS = "VIEW_EVENTS"
    const val INSERT_EVENTS = "INSERT_EVENTS"

    const val REMOVE_VIDEOS = "REMOVE_VIDEOS"
    const val VIEW_VIDEOS = "VIEW_VIDEOS"
    const val VIEW_ANY_VIDEO = "VIEW_ANY_VIDEO"
    const val DOWNLOAD_VIDEOS = "DOWNLOAD_VIDEOS"
    const val VIEW_DISABLED_VIDEOS = "VIEW_DISABLED_VIDEOS"
    const val INSERT_VIDEOS = "INSERT_VIDEOS"
    const val UPDATE_VIDEOS = "UPDATE_VIDEOS"
    const val RATE_VIDEOS = "RATE_VIDEOS"
    const val DOWNLOAD_TRANSCRIPT = "DOWNLOAD_TRANSCRIPT"

    const val INSERT_COLLECTIONS = "INSERT_COLLECTIONS"
    const val VIEW_COLLECTIONS = "VIEW_COLLECTIONS"
    const val VIEW_ANY_COLLECTION = "VIEW_ANY_COLLECTION"
    const val UPDATE_COLLECTIONS = "UPDATE_COLLECTIONS"
    const val DELETE_COLLECTIONS = "DELETE_COLLECTIONS"

    const val REBUILD_SEARCH_INDEX = "REBUILD_SEARCH_INDEX"

    const val INSERT_CONTENT_PARTNERS = "INSERT_CONTENT_PARTNERS"
    const val UPDATE_CONTENT_PARTNERS = "UPDATE_CONTENT_PARTNERS"
    const val VIEW_CONTENT_PARTNERS = "VIEW_CONTENT_PARTNERS"

    const val INSERT_DISCIPLINES = "INSERT_DISCIPLINES"
    const val UPDATE_DISCIPLINES = "UPDATE_DISCIPLINES"
    const val VIEW_DISCIPLINES = "VIEW_DISCIPLINES"
    const val VIEW_DISTRIBUTION_METHODS = "VIEW_DISTRIBUTION_METHODS"

    const val TAG_VIDEOS = "TAG_VIDEOS"
    const val INSERT_TAGS = "INSERT_TAGS"
    const val DELETE_TAGS = "DELETE_TAGS"
    const val VIEW_TAGS = "VIEW_TAGS"
}
