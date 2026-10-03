package com.example.ragcommon

/**
 * Retrieval guardrail: don't pass empty/low-similarity results to LLM.
 */
object RetrievalGuard {
    fun passes(results: List<String>, threshold: Double = 0.7): Boolean =
        results.isNotEmpty() && results.all { it.isNotBlank() }
}
