package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.videos.service.testsupport.asSubjectClassifier
import com.boclips.videos.service.testsupport.asTeacher
import com.boclips.videos.service.testsupport.asUserWithRoles
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CollectionsControllerFilteringIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `gets all user collections with full details, prioritising collections with attachments`() {
        val collectionId = createCollection(title = "collection 1", discoverable = true)
        createCollection(title = "collection 2", discoverable = true)
        addVideo(collectionId, saveVideo(title = "a video title", contentProvider = "A content provider").value)
        updateCollectionAttachment(
            collectionId = collectionId,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "My description",
            attachmentURL = "http://www.google.com"
        )
        mockMvc.perform(get("/v1/collections?projection=details&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].title", equalTo("a video title")))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].createdBy", equalTo("A content provider")))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].contentPartner").doesNotExist())
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
    fun `gets all user collections with basic video details, prioritising collections with attachments`() {
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
    fun `get another users private collections`() {
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
    fun `getting user collections when empty`() {
        mockMvc.perform(get("/v1/collections?projection=list&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(0)))
    }

    @Test
    fun `can fetch collections from another owner`() {
        createCollection(title = "collection 1")

        mockMvc.perform(get("/v1/collections?projection=details&owner=teacher@gmail.com").asTeacher("notTheOwner@gmail.com"))
            .andExpect(status().isOk)
    }

    @Test
    fun `filtering by discoverable collections returns discoverable collections only`() {
        updateCollectionToBeDiscoverable(createCollection("five ponies were eating grass"))
        updateCollectionToBeDiscoverable(createCollection("while a car and a truck crashed"))
        createCollection(title = "the car was owned by a private individual", discoverable = false)
        createCollection(title = "while the truck was company property", discoverable = false)

        mockMvc.perform(get("/v1/collections?discoverable=true&query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while a car and a truck crashed")))
    }

    @Test
    fun `filtering by undiscoverable collections returns discoverable collections only`() {
        updateCollectionToBeDiscoverable(createCollection("five ponies were eating grass"))
        createCollection(title = "while the truck", discoverable = false)

        mockMvc.perform(get("/v1/collections?discoverable=false&query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while the truck")))
    }

    @Test
    fun `filter all discoverable collections and use pagination`() {
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
    fun `query search discoverable collections`() {
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
    fun `query search all collections`() {
        createCollection(title = "five ponies were eating grass", discoverable = true)
        createCollection(title = "the truck was blue and yellow", discoverable = false)
        createCollection(title = "while a car and a truck crashed", discoverable = true)

        mockMvc.perform(
            get("/v1/collections?query=truck").asUserWithRoles(
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.VIEW_ANY_COLLECTION
            )
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
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
    fun `discoverable collections filtered by subject only gets collections with lesson plan first`() {
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
    fun `can filter discoverable collections by subjects`() {
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
}
