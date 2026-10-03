package com.example.ragcommon

/**
 * Shared eval harness: same QA pairs across modules for comparable scores.
 */
object EvalHarness {
    data class QaPair(val q: String, val expected: String)
    val pairs = listOf(
        QaPair("What is RAG?", "Retrieval + generation."),
        QaPair("Why chunk overlap?", "Preserves cross-boundary sentences."),
        QaPair("Embedding model?", "nvidia/nemotron-3-embed-1b:free"),
        QaPair("Similarity threshold?", "0.7"),
        QaPair("Chunk size?", "~500 tokens")
    )
}
