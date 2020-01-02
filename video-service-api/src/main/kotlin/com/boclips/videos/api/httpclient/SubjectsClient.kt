package com.boclips.videos.api.httpclient

import com.boclips.videos.api.response.subject.SubjectResource
import feign.RequestLine
import org.springframework.hateoas.Resources

interface SubjectsClient {
    @RequestLine("GET /v1/subjects")
    fun subjects(): Resources<SubjectResource>
}
