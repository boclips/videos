package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.subject.SubjectsResource
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.Logger
import feign.Param
import feign.RequestLine
import feign.RequestTemplate
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger

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
            tokenFactory: TokenFactory? = null
        ): SubjectsClient {
            return Feign.builder()
                .client(OkHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor { template: RequestTemplate ->
                    if (tokenFactory != null) {
                        template.header("Authorization", "Bearer ${tokenFactory.getAccessToken()}")
                    }
                }
                .logLevel(Logger.Level.BASIC)
                .logger(Slf4jLogger())
                .target(SubjectsClient::class.java, apiUrl)
        }
    }
}
