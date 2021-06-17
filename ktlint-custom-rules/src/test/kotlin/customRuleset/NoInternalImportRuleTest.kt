package customRuleset

import com.pinterest.ktlint.core.*
import com.pinterest.ktlint.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

class NoInternalImportRuleTest {
    @Test
    fun noWildcardImportsRule() {
        assertThat(
            NoInternalImportRule().lint(
                """
                    one/contentpartner/one.kt
                """.trimIndent(),
                """
                    import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
                    import com.boclips.contentpartner.service.domain.model.suggestions.ChannelSuggestion
                """.trimIndent()
            )
        ).containsExactly(
            LintError(
                1, 1, "no-internal-import",
                "You should not import from video service"
            )
        )
    }
}
