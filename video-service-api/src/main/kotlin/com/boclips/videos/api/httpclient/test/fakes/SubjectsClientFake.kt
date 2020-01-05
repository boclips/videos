package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.subject.SubjectCollectionResource
import com.boclips.videos.api.response.subject.SubjectsResource

class SubjectsClientFake : SubjectsClient, FakeClient<SubjectResource> {
    private val database: MutableMap<String, SubjectResource> = LinkedHashMap()
    private var id = 0

    override fun getSubjects(): SubjectsResource {
        return SubjectsResource(_embedded = SubjectCollectionResource(subjects = database.values.toList()), _links = null)
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
        val id = "${id++}"
        database[id] = SubjectResource(id = id, name = createSubjectRequest.name)
    }

    override fun add(subjectResource: SubjectResource): SubjectResource {
        val id = "${id++}"
        database[id] = subjectResource
        return database[id]!!
    }

    override fun clear() {
        database.clear()
    }
}
