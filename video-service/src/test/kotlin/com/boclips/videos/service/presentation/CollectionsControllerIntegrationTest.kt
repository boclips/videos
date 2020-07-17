package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asSubjectClassifier
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CollectionsControllerIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `create a collection`() {
        val firstVideoId = saveVideo(title = "first").value
        val secondVideoId = saveVideo(title = "second").value

        val math = saveSubject("Math")
        val physics = saveSubject("Physics")

        val collectionUrl = mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "a collection",
                        "description": "a description",
                        "videos": ["$firstVideoId", "$secondVideoId"],
                        "discoverable": true,
                        "subjects": ["${math.id.value}", "${physics.id.value}"]
                    }
                    """.trimIndent()
                )
                .asTeacher()
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.containsString("/collections/")))
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(MockMvcRequestBuilders.get(collectionUrl).asTeacher())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.not(Matchers.emptyString())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.owner", Matchers.equalTo("teacher@gmail.com")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy", Matchers.equalTo("Teacher")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.equalTo("a collection")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.equalTo("a description")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.videos", Matchers.hasSize<Any>(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.ageRange").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.subjects", Matchers.hasSize<Any>(2)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.subjects[*].id",
                    Matchers.containsInAnyOrder(math.id.value, physics.id.value)
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.discoverable", Matchers.equalTo(true)))
    }

    @Test
    fun `creating a collection respects projection`() {
        val firstVideoId = saveVideo(title = "first").value

        val createContent = """
                    {
                        "title": "a new collection",
                        "description": "a description",
                        "videos": ["$firstVideoId"],
                        "discoverable": true
                    }
                    """.trimIndent()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content(createContent)
                .asApiUser()
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.videos[0].channelId").doesNotExist())

        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content(createContent)
                .asBoclipsEmployee()
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.videos[0].channelId").exists())
    }

    @Test
    fun `reject creating a collection with null elements in collection fields`() {
        val listFieldNames = listOf(
            "videos",
            "subjects"
        )

        val makeInvalidRequest = { fieldName: String ->
            mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/collections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "random name",
                                "$fieldName": [null]
                            }
                            """.trimIndent()
                    )
                    .asBoclipsEmployee()
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        listFieldNames.map { makeInvalidRequest(it) }
    }

    @Test
    fun `get undiscoverable collections of owner with basic video details`() {
        val collectionId = createCollection(title = "collection 1", discoverable = true)
        createCollection(title = "collection 2", discoverable = false)

        val savedVideoId = saveVideo(title = "a video title")
        addVideo(collectionId, savedVideoId.value)
        updateCollectionAttachment(
            collectionId = collectionId,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "My description",
            attachmentURL = "http://www.google.com"
        )

        mockMvc.perform(get("/v1/collections?projection=list&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(true)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].id", equalTo(savedVideoId.value)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.self.href", endsWith(collectionId)))
            .andExpect(jsonPath("$._embedded.collections[0]._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.details.href").exists())
            .andExpect(jsonPath("$._links.list.href").exists())
            .andReturn()
    }

    @Test
    fun `get discoverable collections of another owner`() {
        val savedVideoId = saveVideo()
        val collectionId = createCollection(discoverable = true)
        addVideo(collectionId, savedVideoId.value)

        mockMvc.perform(get("/v1/collections?projection=list&owner=teacher@gmail.com").asSubjectClassifier())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(false)))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
    }

    @Test
    fun `no collections found`() {
        mockMvc.perform(get("/v1/collections?projection=list&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(0)))
    }

    @Test
    fun `filtering by collections returns discoverable collections by default`() {
        updateCollectionToBeDiscoverable(createCollection("five ponies were eating grass"))
        updateCollectionToBeDiscoverable(createCollection("while a car and a truck crashed"))
        createCollection(title = "the car was owned by a private individual", discoverable = false)
        createCollection(title = "while the truck was company property", discoverable = false)

        mockMvc.perform(get("/v1/collections?query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while a car and a truck crashed")))
    }

    @Test
    fun `can filter by undiscoverable collections when specified explicitly`() {
        updateCollectionToBeDiscoverable(createCollection("five ponies were eating grass"))
        createCollection(title = "while the truck", discoverable = false)

        mockMvc.perform(get("/v1/collections?discoverable=false&query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while the truck")))
    }

    @Test
    fun `filter collections and use pagination`() {
        val collectionId = createCollection("collection 1")
        updateCollectionToBeDiscoverable(collectionId)
        updateCollectionToBeDiscoverable(createCollection("collection 2"))
        updateCollectionAttachment(
            collectionId = collectionId,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "My description",
            attachmentURL = "http://www.google.com"
        )

        mockMvc.perform(get("/v1/collections?projection=list&page=0&size=1").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(false)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next.href").exists())
            .andExpect(jsonPath("$.page.size", equalTo(1)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(2)))
            .andExpect(jsonPath("$.page.totalPages", equalTo(2)))
            .andExpect(jsonPath("$.page.number", equalTo(0)))

            .andExpect(jsonPath("$._embedded.collections[0]._links.bookmark.href").exists())
            .andExpect(jsonPath("$._embedded.collections[0]._links.unbookmark").doesNotExist())

        mockMvc.perform(get("/v1/collections?projection=list&page=1&size=1").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 2")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next").doesNotExist())
    }

    @Test
    fun `filter for age range min and max`() {
        val lowerCollectionId = createCollection("lower")
        val upperCollectionId = createCollection("upper")
        updateCollectionAgeRange(lowerCollectionId, ageRangeMin = 3, ageRangeMax = 5)
        updateCollectionAgeRange(upperCollectionId, ageRangeMin = 5, ageRangeMax = 7)
        updateCollectionToBeDiscoverable(lowerCollectionId)
        updateCollectionToBeDiscoverable(upperCollectionId)

        mockMvc.perform(
            get("/v1/collections?projection=list&page=0&size=5&age_range_min=5&age_range_max=7").asTeacher(
                email = "notTheOwner@gmail.com"
            )
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", equalTo(upperCollectionId)))
    }

    @Test
    fun `filter for age range`() {
        val lowerCollectionId = createCollection("lower")
        val upperCollectionId = createCollection("upper")
        updateCollectionAgeRange(lowerCollectionId, ageRangeMin = 3, ageRangeMax = 4)
        updateCollectionAgeRange(upperCollectionId, ageRangeMin = 5, ageRangeMax = 7)
        updateCollectionToBeDiscoverable(lowerCollectionId)
        updateCollectionToBeDiscoverable(upperCollectionId)

        mockMvc.perform(
            get("/v1/collections?projection=list&page=0&size=5&age_range=5-7&age_range=50-55").asTeacher(
                email = "notTheOwner@gmail.com"
            )
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", equalTo(upperCollectionId)))
    }

    @Test
    fun `query search collections`() {
        updateCollectionToBeDiscoverable(createCollection("five ponies were eating grass"))
        updateCollectionToBeDiscoverable(createCollection("while a car and a truck crashed"))
        createCollection(title = "the truck was blue and yellow", discoverable = true)

        mockMvc.perform(get("/v1/collections?query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while a car and a truck crashed")))
    }

    @Test
    fun `can filter collections by multiple subjects with commas`() {
        val frenchCollection = createCollectionWithTitle("French Collection for with Subjects")
        val germanCollection = createCollectionWithTitle("German Collection for with Subjects")
        val collectionWithoutSubjects = createCollectionWithTitle("My Collection for without Subjects")

        val frenchSubject = saveSubject("French")
        val germanSubject = saveSubject("German")

        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(frenchCollection)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${frenchSubject.id.value}"]}""").asTeacher()
        )
        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(germanCollection)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${germanSubject.id.value}"]}""").asTeacher()
        )

        updateCollectionToBeDiscoverable(frenchCollection)
        updateCollectionToBeDiscoverable(germanCollection)
        updateCollectionToBeDiscoverable(collectionWithoutSubjects)

        mockMvc.perform(
            get("/v1/collections?subject=${frenchSubject.id.value},${germanSubject.id.value}")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
    }

    @Test
    fun `collections filtered by subject and sorted by attachment`() {
        val collectionWithLessonPlan1 = createCollectionWithTitle("With lesson plan 1")
        val collectionWithoutLessonPlan = createCollectionWithTitle("No lesson plan")
        val collectionWithLessonPlan2 = createCollectionWithTitle("With lesson plan 2")

        val subject = saveSubject("subject")
        val attachment = AttachmentFactory.sample()

        listOf(collectionWithLessonPlan1, collectionWithLessonPlan2).forEach {
            collectionRepository.update(
                CollectionUpdateCommand.AddAttachment(
                    CollectionId(it),
                    attachment.description,
                    attachment.linkToResource,
                    AttachmentType.LESSON_PLAN,
                    UserFactory.sample()
                )
            )
        }

        listOf(collectionWithLessonPlan1, collectionWithoutLessonPlan, collectionWithLessonPlan2).forEach {
            updateCollectionToBeDiscoverable(it)
            mockMvc.perform(
                MockMvcRequestBuilders.patch(selfLink(it)).contentType(MediaType.APPLICATION_JSON)
                    .content("""{"subjects": ["${subject.id.value}"]}""").asTeacher()
            )
        }

        mockMvc.perform(
            get("/v1/collections?subject=${subject.id.value}&sort_by=HAS_ATTACHMENT")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(3)))
            .andExpect(jsonPath("$._embedded.collections[0].title", startsWith("With lesson plan")))
            .andExpect(jsonPath("$._embedded.collections[1].title", startsWith("With lesson plan")))
            .andExpect(jsonPath("$._embedded.collections[2].title", equalTo("No lesson plan")))
    }

    @Test
    fun `can filter collections by subjects`() {
        val frenchCollection = createCollectionWithTitle("My Collection for with Subjects")
        val unclassifiedCollection = createCollectionWithTitle("My Collection for without Subjects")

        val frenchSubject = saveSubject("French")

        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(frenchCollection)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${frenchSubject.id.value}"]}""").asTeacher()
        )

        updateCollectionToBeDiscoverable(frenchCollection)
        updateCollectionToBeDiscoverable(unclassifiedCollection)

        mockMvc.perform(
            get("/v1/collections?subject=${frenchSubject.id.value}&projection=details")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
    }

    @Test
    fun `can filter collections by resource type`() {
        val collectionWithLessonPlan = createCollectionWithTitle("My Collection with lesson guide")
        val collectionWithoutLessonPlan = createCollectionWithTitle("My Collection without lesson guide")

        updateCollectionAttachment(
            collectionId = collectionWithLessonPlan,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "My lesson plan",
            attachmentURL = "http://www.boclips.com"
        )
        listOf(collectionWithLessonPlan, collectionWithoutLessonPlan).forEach {
            updateCollectionToBeDiscoverable(it)
        }

        mockMvc.perform(
            get("/v1/collections?resource_types=LESSON_PLAN").asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
    }

    @Test
    fun `can filter collections by multiple resource types`() {
        val collectionWithLessonPlan = createCollectionWithTitle("My Collection with lesson guide")
        val collectionWithActivity = createCollectionWithTitle("My Collection with activity")

        updateCollectionAttachment(
            collectionId = collectionWithLessonPlan,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "My lesson plan",
            attachmentURL = "http://www.boclips.com"
        )

        updateCollectionAttachment(
            collectionId = collectionWithActivity,
            attachmentType = "ACTIVITY",
            attachmentDescription = "My activity",
            attachmentURL = "http://www.boclips.com"
        )

        listOf(collectionWithLessonPlan, collectionWithActivity).forEach {
            updateCollectionToBeDiscoverable(it)
        }

        mockMvc.perform(
                get("/v1/collections?resource_types=LESSON_PLAN,ACTIVITY").asTeacher("teacher@gmail.com")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
    }

    @Test
    fun `can filter by multiple sort criteria`() {
        mockMvc.perform(
            get("/v1/collections?sort_by=HAS_ATTACHMENT,IS_DEFAULT")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            get("/v1/collections?sort_by=HAS_ATTACHMENT&sort_by=IS_DEFAULT")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `get all promoted collections`() {
        val promotedCollectionIds: Array<String> = (1..10).asIterable().map {
            createCollection(title = "collection$it", discoverable = true).apply {
                updateCollectionToBePromoted(this)
            }
        }.toTypedArray()

        (1..10).asIterable().map {
            createCollection("collection$it")
        }

        mockMvc.perform(get("/v1/collections?projection=list&page=0&size=30&promoted=true").asTeacher(email = "random@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(10)))
            .andExpect(jsonPath("$._embedded.collections[*].id", Matchers.hasItems(*promotedCollectionIds)))
    }

    @Test
    fun `get bookmarked collections correctly paginated`() {
        createCollection(title = "collection 1", discoverable = true).apply {
            bookmarkCollection(this, "notTheOwner@gmail.com")
        }
        createCollection(title = "collection 2", discoverable = true).apply {
            bookmarkCollection(this, "notTheOwner@gmail.com")
        }

        mockMvc.perform(
            get("/v1/collections?projection=list&page=0&size=1&bookmarked=true")
                .asTeacher(email = "notTheOwner@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(Matchers.emptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(false)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next.href").exists())
            .andExpect(jsonPath("$._embedded.collections[0]._links.unbookmark.href").exists())
            .andExpect(jsonPath("$._embedded.collections[0]._links.bookmark").doesNotExist())

        mockMvc.perform(get("/v1/collections?projection=list&page=1&size=1&bookmarked=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 2")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next").doesNotExist())
    }

    @Test
    fun `get collections filtered by lesson plan as attachment`() {
        val collectionWithLessonPlan =
            createCollection(
                title = "collection with lesson plans",
                description = "collection with a LESSON_PLAN attachment",
                discoverable = true
            )

        createCollection("collection without lesson plans", discoverable = true)

        updateCollectionAttachment(
            collectionId = collectionWithLessonPlan,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "collection with lesson plan",
            attachmentURL = "http://www.google.com"
        )

        mockMvc.perform(
            get("/v1/collections?query=collection&has_lesson_plans=true").asApiUser()
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(
                jsonPath(
                    "$._embedded.collections[0].description",
                    equalTo("collection with a LESSON_PLAN attachment")
                )
            )
    }

    @Test
    fun `get collections without lesson plans`() {
        val collectionWithLessonPlan =
            createCollection(
                title = "collection with lesson plans",
                description = "collection with a LESSON_PLAN attachment",
                discoverable = true
            )

        updateCollectionAttachment(
            collectionId = collectionWithLessonPlan,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "collection with lesson plan",
            attachmentURL = "http://www.google.com"
        )

        createCollection("collection without lesson plans 1", discoverable = true)
        createCollection("collection without lesson plans 2", discoverable = true)

        mockMvc.perform(
            get("/v1/collections?query=collection&has_lesson_plans=false").asApiUser()
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(
                jsonPath(
                    "$._embedded.collections[0].title",
                    equalTo("collection without lesson plans 1")
                )
            ).andExpect(
                jsonPath(
                    "$._embedded.collections[1].title",
                    equalTo("collection without lesson plans 2")
                )
            )
    }

    @Test
    fun `returns all collection for backoffice user requesting all collections`() {
        createCollection(title = "collection 1", discoverable = true)
        createCollection(title = "collection 2", discoverable = false)

        mockMvc.perform(get("/v1/collections?ignore_discoverable=true").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))

        mockMvc.perform(get("/v1/collections?ignore_discoverable=true").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
    }
}
