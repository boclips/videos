package com.boclips.videos.service.domain.model.asset

import org.bson.types.ObjectId

class AssetId(value: String) {
    val value: String

    init {
        try {
            ObjectId(value)
            this.value = value
        } catch (e: IllegalArgumentException) {
            throw IllegalVideoIdentifier("$value is not a valid ID")
        }
    }

    override fun toString(): String {
        return this.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetId

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
