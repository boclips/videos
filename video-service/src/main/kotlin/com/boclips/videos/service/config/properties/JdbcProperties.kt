package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jdbc")
class JdbcProperties {
    companion object {
        const val JDBC_MYSQL_STREAMING_HINT = Integer.MIN_VALUE
    }

    var fetchSize: Int = JDBC_MYSQL_STREAMING_HINT
}