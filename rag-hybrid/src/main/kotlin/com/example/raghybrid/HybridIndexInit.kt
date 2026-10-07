package com.example.raghybrid
import com.example.ragcommon.Chunker
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.nio.file.Paths
@Component
class HybridIndexInit(private val hybrid: HybridRetrieval, private val eval: HybridEval) : CommandLineRunner {
    override fun run(vararg args: String) {
        println("Building Lucene BM25 index...")
        val chunks = Chunker.loadAndChunk(Paths.get("rag-naive/test-data"))
        hybrid.buildIndex(chunks.map { it.text ?: "" })
        println("Index built: ${chunks.size} chunks")
        // Step 5: run eval loop
        eval.run()
    }
}
