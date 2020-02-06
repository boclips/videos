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
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
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
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
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
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
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
        val collectionId = createCollection()
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
    fun `cannot fetch private collection when owner does not match user`() {
        createCollection("collection 1")

        mockMvc.perform(get("/v1/collections?projection=details&owner=teacher@gmail.com&public=false").asTeacher("notTheOwner@gmail.com"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `searching for public collections returns public collections only`() {
        updateCollectionToBePublic(createCollection("five ponies were eating grass"))
        updateCollectionToBePublic(createCollection("while a car and a truck crashed"))
        createCollection(title = "the car was owned by a private individual", public = false)
        createCollection(title = "while the truck was company property", public = false)

        mockMvc.perform(get("/v1/collections?public=true&query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while a car and a truck crashed")))
    }

    @Test
    fun `filter all public collections and use pagination`() {
        val collectionId = createCollection("collection 1")
        updateCollectionToBePublic(collectionId)
        updateCollectionToBePublic(createCollection("collection 2"))
        updateCollectionAttachment(
            collectionId = collectionId,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "My description",
            attachmentURL = "http://www.google.com"
        )

        mockMvc.perform(get("/v1/collections?projection=list&page=0&size=1&public=true").asTeacher(email = "notTheOwner@gmail.com"))
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

        mockMvc.perform(get("/v1/collections?projection=list&page=1&size=1&public=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 2")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next").doesNotExist())
    }
    @Test
    fun `filter for age range`() {
        val lowerCollectionId = createCollection("lower")
        val upperCollectionId = createCollection("upper")
        updateCollectionAgeRange(lowerCollectionId, ageRangeMin = 3, ageRangeMax = 5)
        updateCollectionAgeRange(upperCollectionId, ageRangeMin = 5, ageRangeMax = 7)
        updateCollectionToBePublic(lowerCollectionId)
        updateCollectionToBePublic(upperCollectionId)

        mockMvc.perform(get("/v1/collections?projection=list&page=0&size=5&public=true&age_range_min=5&age_range_max=7").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", equalTo(upperCollectionId)))
    }

    @Test
    fun `query search public collections`() {
        updateCollectionToBePublic(createCollection("five ponies were eating grass"))
        updateCollectionToBePublic(createCollection("while a car and a truck crashed"))
        createCollection(title = "the truck was blue and yellow", public = true)

        mockMvc.perform(get("/v1/collections?public=true&query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while a car and a truck crashed")))
    }

    @Test
    fun `query search all collections`() {
        createCollection(title = "five ponies were eating grass", public = true)
        createCollection(title = "the truck was blue and yellow", public = false)
        createCollection(title = "while a car and a truck crashed", public = true)

        mockMvc.perform(
            get("/v1/collections?query=truck").asUserWithRoles(
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.VIEW_ANY_COLLECTION
            )
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("the truck was blue and yellow")))
            .andExpect(jsonPath("$._embedded.collections[1].title", equalTo("while a car and a truck crashed")))
    }

    @Test
    fun `query search for my collections and my bookmarked collections sorted alphabetically`() {
        val teacher = "teacher"
        val stranger = "stranger"

        createCollection(title = "mine", public = true, owner = teacher)
        createCollection(title = "strangers", public = true, owner = stranger)
        createCollection(title = "another collection", public = true, owner = teacher)
        createCollection(title = "bookmarked", public = true, owner = stranger).apply {
            bookmarkCollection(this, teacher)
        }

        mockMvc.perform(
            get("/v1/collections?owner=$teacher&bookmarked=true&sort_by=TITLE").asTeacher(teacher)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(3)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("another collection")))
            .andExpect(jsonPath("$._embedded.collections[1].title", equalTo("bookmarked")))
            .andExpect(jsonPath("$._embedded.collections[2].title", equalTo("mine")))
    }

    @Test
    fun `can filter public collections by multiple subjects with commas`() {
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

        updateCollectionToBePublic(frenchCollection)
        updateCollectionToBePublic(germanCollection)
        updateCollectionToBePublic(collectionWithoutSubjects)

        mockMvc.perform(
            get("/v1/collections?subject=${frenchSubject.id.value},${germanSubject.id.value}&public=true")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
    }

    @Test
    fun `public collections filtered by subject only gets collections with lesson plan first`() {
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
            updateCollectionToBePublic(it)
            mockMvc.perform(
                MockMvcRequestBuilders.patch(selfLink(it)).contentType(MediaType.APPLICATION_JSON)
                    .content("""{"subjects": ["${subject.id.value}"]}""").asTeacher()
            )
        }

        mockMvc.perform(
            get("/v1/collections?subject=${subject.id.value}&public=true")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(3)))
            .andExpect(jsonPath("$._embedded.collections[0].title", startsWith("With lesson plan")))
            .andExpect(jsonPath("$._embedded.collections[1].title", startsWith("With lesson plan")))
            .andExpect(jsonPath("$._embedded.collections[2].title", equalTo("No lesson plan")))
    }

    @Test
    fun `can filter public collections by subjects`() {
        val frenchCollection = createCollectionWithTitle("My Collection for with Subjects")
        val unclassifiedCollection = createCollectionWithTitle("My Collection for without Subjects")

        val frenchSubject = saveSubject("French")

        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(frenchCollection)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${frenchSubject.id.value}"]}""").asTeacher()
        )

        updateCollectionToBePublic(frenchCollection)
        updateCollectionToBePublic(unclassifiedCollection)

        mockMvc.perform(
            get("/v1/collections?subject=${frenchSubject.id.value}&projection=details&public=true")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
    }
}
