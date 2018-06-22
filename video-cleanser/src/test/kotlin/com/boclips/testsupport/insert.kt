package com.boclips.testsupport

fun insert(id: String? = null, referenceId: String? = null, title: String? = "some title", contentProvider: String? = "some cp"): String {
    fun stringifyIfSet(string: String?): String? {
        if (string != null) return "'$string'"
        return null
    }

    return """
        INSERT INTO metadata_orig(id, reference_id, title, source)
        VALUES(
            ${stringifyIfSet(id)},
            ${stringifyIfSet(referenceId)},
            ${stringifyIfSet(title)},
            ${stringifyIfSet(contentProvider)}
        )"""
}