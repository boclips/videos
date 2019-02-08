package com.boclips.videos.service.infrastructure.email

class NoResultsEmail(val name: String?, val email: String?, val query: String?, val description: String?) {
    fun toPlainText(): String? {
        return "" +
            "Name: $name\n" +
            "Email: $email\n" +
            "Query: $query\n" +
            "Description: $description"
    }
}
