package com.example.naiverag

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import com.example.ragcommon.PromptBuilder
import com.example.ragcommon.FaithfulnessGuard
import org.springframework.web.bind.annotation.RestController

/**
 * Simple REST endpoint to test the full pipeline via curl.
 * Keeps retrieval, prompt construction, and generation visible and separate.
 */
@RestController
class RagController(
    private val retrievalService: RetrievalService,
    
    private val generationService: GenerationService,
    private val chatClient: org.springframework.ai.chat.client.ChatClient
) {

    @GetMapping("/ask")
    fun ask(@RequestParam q: String): Map<String, Any> {
        val chunks = retrievalService.retrieve(q)
        val answer = if (chunks.isEmpty()) {
            "I don't have enough information to answer that"
        } else {
            val prompt = PromptBuilder.build(q, chunks)
            generationService.generate(prompt)
        }
        val faithful = if (chunks.isNotEmpty()) FaithfulnessGuard.check(answer, chunks.joinToString("\n"), chatClient) else false
        return mapOf(
            "query" to q,
            "retrievedChunks" to chunks.size,
            "answer" to answer,
            "faithful" to faithful
        )
    }
}
