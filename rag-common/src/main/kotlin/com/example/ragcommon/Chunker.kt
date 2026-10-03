package com.example.ragcommon

import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import java.nio.file.Files
import java.nio.file.Path

/**
 * Shared chunking (~500 tokens, overlap handled by splitter).
 * Every module uses identical chunk IDs because RRF needs them matched
 * between Lucene index and Chroma vectors.
 */
object Chunker {
    private val splitter = TokenTextSplitter.builder()
        .withChunkSize(500)
        .build()

    fun loadAndChunk(directory: Path): List<Document> {
        val docs = mutableListOf<Document>()
        Files.list(directory).use { stream ->
            stream.filter { it.toString().endsWith(".md") || it.toString().endsWith(".txt") }
                .forEach { path ->
                    val text = Files.readString(path)
                    val chunks = splitter.split(Document(text, mapOf("source" to path.fileName.toString())))
                    docs.addAll(chunks)
                }
        }
        return docs
    }
}
