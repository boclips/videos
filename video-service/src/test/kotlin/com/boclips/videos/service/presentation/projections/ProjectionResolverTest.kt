package com.boclips.videos.service.presentation.projections

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PublicApiProjection
import com.boclips.videos.service.config.security.UserRoles.API
import com.boclips.videos.service.config.security.UserRoles.BOCLIPS_SERVICE
import com.boclips.videos.service.config.security.UserRoles.HQ
import com.boclips.videos.service.config.security.UserRoles.PUBLISHER
import com.boclips.videos.service.config.security.UserRoles.TEACHER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import kotlin.reflect.KClass

class ProjectionResolverTest {

    companion object {
        val testCases = Stream.of(
            arrayOf(API) to PublicApiProjection::class,
            arrayOf(PUBLISHER) to BoclipsInternalProjection::class,
            arrayOf(HQ) to BoclipsInternalProjection::class,
            arrayOf(HQ, TEACHER) to BoclipsInternalProjection::class,
            arrayOf(PUBLISHER, API) to BoclipsInternalProjection::class,
            arrayOf(BOCLIPS_SERVICE) to BoclipsInternalProjection::class,
            emptyArray<String>() to PublicApiProjection::class
        )
    }

    @ParameterizedTest
    @ArgumentsSource(ProjectionResolverProvider::class)
    fun `maps role(s) to projection`(roles: Array<String>, projection: KClass<*>) {
        setSecurityContext("user", *roles)

        assertThat(RoleBasedProjectionResolver().resolveProjection()).isEqualTo(projection.java)
    }

    class ProjectionResolverProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<Arguments>? {
            return ProjectionResolverTest.testCases.map { Arguments.of(it.first, it.second) }
        }
    }
}
