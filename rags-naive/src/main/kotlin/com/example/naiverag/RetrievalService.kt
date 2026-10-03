package com.example.naiverag

import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * Step 3: Query + retrieval with guardrail.
 *
 * Why separate: retrieval quality is the single biggest lever in this naive
 * pipeline. Making it independently callable lets us log which chunks were
 * retrieved for every query (see RagController / EvalService).
 *
 * What we do:
 *  1. Build a SearchRequest with topK=5 and similarityThreshold=0.7.
 *  2. Call vectorStore.similaritySearch(request).
 *  3. If results come back empty (below threshold), short-circuit — do NOT pass
 *     empty/irrelevant context to the LLM. Instead return an empty list so
 *     RagController can reply "I don't have enough information to answer that".
 */
@Service
class RetrievalService(private val vectorStore: VectorStore) {

    fun retrieve(query: String): List<String> {
        // Build the retrieval request with explicit guardrail settings.
        val request = SearchRequest.builder().query(query)
            .topK(5)
            .similarityThreshold(0.7) //with 0.3 I get results but with 0.7 I get empty results
            .build()

        // Execute dense similarity search against Chroma.
        val results = vectorStore.similaritySearch(request)

        // Guardrail: if nothing passes the 0.7 threshold, return empty.
        // This prevents the LLM from hallucinating over missing context.
        if (results.isEmpty()) {
            return emptyList()
        }

        // Log/retrieve only the text so prompt construction can join chunks.
        return results.map { doc -> doc.text ?: "" }
    }
}
