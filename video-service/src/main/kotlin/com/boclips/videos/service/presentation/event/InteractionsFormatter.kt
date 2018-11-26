package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.infrastructure.event.analysis.Interaction
import com.boclips.videos.service.infrastructure.event.types.User

object InteractionsFormatter {
    fun format(interactions: List<Interaction>): String {
        return interactions.reversed()
                .flatMap {
                    listOf(">  ${format(it)} by ${format(it.user)}") +
                            it.related.map { "   ${format(it)}" }
                }
                .joinToString("\n")
    }

    private fun format(user: User) = if( user.boclipsEmployee ) "Boclips employee" else "teacher"

    private fun format(interaction: Interaction): String {
        return "${interaction.timestamp} ${interaction.description}"
    }
}
