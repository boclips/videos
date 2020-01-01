package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.response.subject.SubjectResource
import org.springframework.hateoas.Resources

class SubjectsClientFake : SubjectsClient {
    override fun subjects(): Resources<SubjectResource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}