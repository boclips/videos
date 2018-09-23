package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.infrastructure.event.analysis.Interaction

object InteractionsFormatter {

    fun format(interactions: List<Interaction>): String {
        return interactions.reversed()
                .flatMap { listOf(">  ${format(it)}") + it.related.map { "   ${format(it)}" } }
                .joinToString("\n")
    }

    private fun format(interaction: Interaction): String {
        return "${interaction.timestamp} ${interaction.description}"
    }
}
