package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.response.subject.SubjectResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources

class SubjectsClientFake : SubjectsClient {
    private val database: MutableMap<String, SubjectResource> = LinkedHashMap()
    private var id = 0

    override fun getSubjects(): Resources<SubjectResource> {
        return Resources(database.values)
    }

    override fun getSubject(id: String): Resource<SubjectResource> {
        return Resource(database[id] ?: error("no such element"))
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

    fun add(subjectResource: SubjectResource) : SubjectResource {
        val id = "${id++}"
        database[id] = subjectResource
        return database[id]!!
    }

    fun clear() {
        database.clear()
    }
}
