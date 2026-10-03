package com.example.naiverag

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Step 2: Embed + store.
 * Calling vectorStore.add in one batch lets the configured EmbeddingModel
 * embed all chunks via the OpenRouter embedding endpoint, then write to Chroma.
 * We keep this separate from retrieval so embedding errors can be debugged
 * independently of query-time retrieval.
 */
@Service
class EmbeddingStoreService(private val vectorStore: VectorStore) {

    fun store(chunkedDocuments: List<Document>) {
        if (chunkedDocuments.isNotEmpty()) {
            vectorStore.add(chunkedDocuments)
        }
    }

}
