package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.subject.SubjectsResource
import com.boclips.videos.api.response.subject.SubjectsWrapperResource

class SubjectsClientFake : SubjectsClient, FakeClient<SubjectResource> {
    private val database: MutableMap<String, SubjectResource> = LinkedHashMap()
    private var id = 0

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
        val resource = SubjectResource(id = "${id++}", name = createSubjectRequest.name)
        database[resource.id] = resource
    }

    override fun add(element: SubjectResource): SubjectResource {
        val resource = element.copy(id = "${id++}")
        database[resource.id] = resource
        return resource
    }

    override fun clear() {
        database.clear()
    }

    override fun findAll(): List<SubjectResource> {
        return database.values.toList()
    }
}
