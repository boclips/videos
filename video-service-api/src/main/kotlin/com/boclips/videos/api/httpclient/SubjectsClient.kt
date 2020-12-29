package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.subject.SubjectsResource
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Client
import feign.Param
import feign.RequestLine

interface SubjectsClient {
    @RequestLine("GET /v1/subjects")
    fun getSubjects(): SubjectsResource

    @RequestLine("GET /v1/subjects/{subjectId}")
    fun getSubject(@Param("subjectId") id: String): SubjectResource

    @RequestLine("DELETE /v1/subjects/{subjectId}")
    fun deleteSubject(@Param("subjectId") id: String)

    @RequestLine("PUT /v1/subjects/{subjectId}")
    fun updateSubject(@Param("subjectId") id: String, createSubjectRequest: CreateSubjectRequest)

    @RequestLine("POST /v1/subjects")
    fun create(createSubjectRequest: CreateSubjectRequest)

    companion object {
        @JvmStatic
        fun create(
            apiUrl: String,
            objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
            tokenFactory: TokenFactory? = null,
            feignClient: Client
        ) = FeignInterserviceClientFactory.create(
                apiUrl,
                objectMapper,
                tokenFactory,
                feignClient,
                SubjectsClient::class.java
        )
    }
}
