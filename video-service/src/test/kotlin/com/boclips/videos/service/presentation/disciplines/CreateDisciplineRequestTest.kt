package com.boclips.videos.service.presentation.disciplines

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import javax.validation.Validation
import javax.validation.Validator

class CreateDisciplineRequestTest {
    lateinit var validator: Validator

    companion object {
        val validRequest = CreateDisciplineRequest(name = "A valid name", code = "valid-code")

        val testCases = Stream.of(
            validRequest.copy(name = null) to "Discipline name is required",
            validRequest.copy(name = "a") to "Discipline name must be between 1 and 100 characters",
            validRequest.copy(name = "X".repeat(101)) to "Discipline name must be between 1 and 100 characters",

            validRequest.copy(code = null) to "Discipline code is required",
            validRequest.copy(code = "Asdf*/") to "Discipline code must contain lower-case letter and/or hyphens only",
            validRequest.copy(code = "a") to "Discipline code must be between 1 and 50 characters",
            validRequest.copy(code = "x".repeat(51)) to "Discipline code must be between 1 and 50 characters"
        )
    }

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    @ParameterizedTest
    @ArgumentsSource(CreateDisciplineRequestProvider::class)
    fun `validates name being null`(request: CreateDisciplineRequest, errorMessage: String) {
        val violations = validator.validate(request)

        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message).isEqualTo(errorMessage)
    }

    class CreateDisciplineRequestProvider : ArgumentsProvider {

        override fun provideArguments(context: ExtensionContext?): Stream<Arguments>? {
            return testCases.map { Arguments.of(it.first, it.second) }
        }
    }
}
