package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.video.CaptionFormatRequest
import java.beans.PropertyEditorSupport

class CaptionFormatRequestEnumConverter : PropertyEditorSupport() {
    override fun setAsText(text: String) {
        value = CaptionFormatRequest.valueOf(text.toUpperCase())
    }
}
