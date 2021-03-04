package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.subject.SubjectsResource
import com.boclips.videos.api.response.subject.SubjectsWrapperResource
import java.util.*
import kotlin.collections.LinkedHashMap

class SubjectsClientFake : SubjectsClient, FakeClient<SubjectResource> {
    private val database: MutableMap<String, SubjectResource> = LinkedHashMap()

    override fun getSubjects(): SubjectsResource {
        return SubjectsResource(_embedded = SubjectsWrapperResource(subjects = database.values.toList()), _links = null)
    }

    override fun getSubject(id: String): SubjectResource {
        return database[id] ?: error("no such element")
    }

    override fun deleteSubject(id: String) {
        database.remove(id)
        return
    }

    override fun updateSubject(id: String, createSubjectRequest: CreateSubjectRequest) {
        database.replace(id, SubjectResource(id = id, name = createSubjectRequest.name))
    }

    override fun create(createSubjectRequest: CreateSubjectRequest) {
        val resource = SubjectResource(id = UUID.randomUUID().toString(), name = createSubjectRequest.name)
        database[resource.id] = resource
    }

    override fun add(element: SubjectResource): SubjectResource {
        if (database[element.id] != null) throw RuntimeException(
            "An element with id ${element.id} already exists. Please create a proper fixture."
        )
        database[element.id] = element
        return element
    }

    override fun clear() {
        database.clear()
    }

    override fun findAll(): List<SubjectResource> {
        return database.values.toList()
    }
}
