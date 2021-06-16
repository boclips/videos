package com.boclips.contentpartner.service.presentation.converters

import com.boclips.videos.api.common.IngestType
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import org.springframework.http.HttpStatus

object IngestTypeConverter {
    fun convertType(type: String): IngestType {
        return try {
            IngestType.valueOf(type)
        } catch (ex: IllegalArgumentException) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    error = "Invalid ingest type provided",
                    message = "The type $type does not exist in the list of valid types (${IngestType.values()})",
                    status = HttpStatus.BAD_REQUEST
                )
            )
        }
    }
}
