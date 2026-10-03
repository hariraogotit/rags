package com.example.naiverag

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Simple REST endpoint to test the full pipeline via curl.
 * Keeps retrieval, prompt construction, and generation visible and separate.
 */
@RestController
class RagController(
    private val retrievalService: RetrievalService,
    private val promptBuilderService: PromptBuilderService,
    private val generationService: GenerationService,
    private val faithfulnessGuardService: FaithfulnessGuardService
) {

    @GetMapping("/ask")
    fun ask(@RequestParam q: String): Map<String, Any> {
        val chunks = retrievalService.retrieve(q)
        val answer = if (chunks.isEmpty()) {
            "I don't have enough information to answer that"
        } else {
            val prompt = promptBuilderService.build(q, chunks)
            generationService.generate(prompt)
        }
        val faithful = if (chunks.isNotEmpty()) faithfulnessGuardService.check(answer, chunks.joinToString("\n")) else false
        return mapOf(
            "query" to q,
            "retrievedChunks" to chunks.size,
            "answer" to answer,
            "faithful" to faithful
        )
    }
}
