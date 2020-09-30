package com.boclips.search.service.infrastructure.videos

data class PhraseParts(
    val unquoted: String,
    val quotedParts: List<String>
)

fun String.quotedParts(): PhraseParts {
    val quoteIndices = this
        .mapIndexed { index, char ->
            Pair(index, char == '"')
        }
        .filter { it.second }
        .map { it.first }
    if (quoteIndices.size < 2) return PhraseParts(
        unquoted = this.filter { it != '"' },
        quotedParts = listOf()
    )
    val validQuoteIndices = quoteIndices.trimIfOdd()
    val quoteIndexPairs = validQuoteIndices.chunked(2) { Pair(it[0], it[1]) }
    val quotedSubstrings =
        quoteIndexPairs.map { (from, to) ->
            this.substring(from + 1, to)
        }
    return PhraseParts(
        unquoted = this.filter {it != '"'},
        quotedParts = quotedSubstrings
    )
}

private fun List<Int>.chunkPairs() =
    chunked(2) { Pair(it[0], it[1]) }

private fun List<Int>.trimIfOdd() =
    if (this.size % 2 == 1)
        this.dropLast(1) else this
