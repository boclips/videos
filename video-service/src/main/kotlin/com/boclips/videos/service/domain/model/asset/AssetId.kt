package com.boclips.videos.service.domain.model.asset

data class AssetId(
        val value: String,
        val alias: String? = null
) {
    override fun toString(): String {
        return "[id = ${this.value}]"
    }
}
