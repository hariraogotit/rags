package com.example.naiverag
import com.example.ragcommon.Chunker
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.nio.file.Paths
@Component
class PipelineInitializer(private val store: EmbeddingStoreService, private val eval: EvalService) : CommandLineRunner {
    override fun run(vararg args: String) {
        println("Loading via rag-common Chunker...")
        val chunks = Chunker.loadAndChunk(Paths.get("rags-naive/test-data"))
        println("Chunks: ${chunks.size}")
        store.store(chunks)
        eval.run()
    }
}
