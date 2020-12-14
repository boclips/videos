package com.boclips.search.service.domain.videos.model

data class VideoMetadataToCSV(
    val id: String?,
    val contentPartner: String?,
    val title: String?,
    val description: String?,
    val duration: String?,
    val videoReference: String?,
    val legalRestrictions: String?,
    val transcripts: Boolean?,
    val links: Any?,
    val keywords: List<String>?,
)
