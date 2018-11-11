package com.boclips.videos.service.domain.model.asset

data class AssetId(
        val value: String
) {
    override fun toString(): String {
        return "[id = ${this.value}]"
    }
}
