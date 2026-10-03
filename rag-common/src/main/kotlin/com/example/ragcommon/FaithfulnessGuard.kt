package com.example.ragcommon

/**
 * Faithfulness guard: ask LLM whether answer is supported by context.
 * Returns lower-confidence message if unsupported rather than asserting false claims.
 */
object FaithfulnessGuard {
    fun check(answer: String, context: String, chatClient: org.springframework.ai.chat.client.ChatClient): Boolean {
        val prompt = "Is this answer supported by the following context? Answer only yes or no.\n\nContext:\n$context\n\nAnswer: $answer"
        val res = chatClient.prompt().user(prompt).call().content() ?: "no"
        return res.contains("yes", ignoreCase = true)
    }
}
