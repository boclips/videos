package com.boclips.videos.service.presentation

import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.subject.MongoSubjectRepository
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.bson.types.ObjectId
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.getCollection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubjectControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val subjectDocuments = listOf(
            SubjectDocument(id = ObjectId(), name = "Mathematics"),
            SubjectDocument(id = ObjectId(), name = "French")
        )

        mongoClient.getDatabase(DATABASE_NAME)
            .getCollection<SubjectDocument>(MongoSubjectRepository.collectionName)
            .insertMany(subjectDocuments)
    }

    @Test
    fun `returns list of subjects`() {
        mockMvc.perform(get("/v1/subjects"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.subjects", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.subjects[0].id").exists())
            .andExpect(jsonPath("$._embedded.subjects[0].name").exists())
            .andExpect(jsonPath("$._links.self.href").exists())
    }
}