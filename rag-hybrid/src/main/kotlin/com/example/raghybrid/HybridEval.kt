package com.example.raghybrid
import com.example.ragcommon.EvalHarness
import org.springframework.stereotype.Service
@Service
class HybridEval(private val hybrid: HybridRetrieval) {
    fun run() {
        println("\n=== Hybrid Eval ===")
        EvalHarness.pairs.forEachIndexed { idx, pair ->
            val chunks = hybrid.retrieve(pair.q)
            println("Q${idx+1}: ${pair.q} | chunks=${chunks.size}")
        }
    }
}
