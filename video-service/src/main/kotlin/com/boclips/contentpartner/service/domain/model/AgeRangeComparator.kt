package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.api.request.contentpartner.AgeRangeRequest

object AgeRangeComparator {
    fun areDifferent(contentPartnerAgeRange: AgeRange, contentPartnerRequestAgeRange: AgeRangeRequest?): Boolean {
        return contentPartnerAgeRange.min() != contentPartnerRequestAgeRange?.min || contentPartnerAgeRange.max() != contentPartnerRequestAgeRange?.max
    }
}