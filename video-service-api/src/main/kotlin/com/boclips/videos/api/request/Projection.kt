package com.boclips.videos.api.request

enum class Projection {
    /**Intended for expensive and non-critical information*/
    full,
    /**Intended for single resource details*/
    details,
    /**Intended for lists or pages of resources*/
    list
}
