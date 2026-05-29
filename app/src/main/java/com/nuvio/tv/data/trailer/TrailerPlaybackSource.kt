package com.nuvio.tv.data.trailer

data class TrailerPlaybackSource(
    val videoUrl: String,
    val audioUrl: String? = null
)

data class TrailerPlaybackResolution(
    val source: TrailerPlaybackSource?,
    val diagnostic: TrailerPlaybackDiagnostic
)

data class TrailerPlaybackDiagnostic(
    val code: String,
    val details: List<String> = emptyList()
) {
    fun toDisplayMessage(baseMessage: String): String {
        val diagnosticLines = buildList {
            add("Diagnostic: $code")
            addAll(details)
        }
        return (listOf(baseMessage) + diagnosticLines)
            .joinToString("\n")
    }
}

data class YouTubeExtractionResult(
    val source: TrailerPlaybackSource?,
    val diagnostic: TrailerPlaybackDiagnostic
)
