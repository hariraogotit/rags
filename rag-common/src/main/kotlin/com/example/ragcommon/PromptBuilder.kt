package com.example.ragcommon

/**
 * Manual prompt template — visible, debuggable, not hidden in an advisor.
 */
object PromptBuilder {
    fun build(query: String, chunks: List<String>): String {
        val context = chunks.joinToString("\n---\n")
        println("[RETRIEVAL] Retrieved ${chunks.size} chunk(s) for: $query")
        chunks.forEachIndexed { i, t -> println("  chunk[$i] (${t.length} chars): ${t.take(120)}...") }
        return "Answer using only this context:\n$context\n\nQuestion: $query"
    }
}
