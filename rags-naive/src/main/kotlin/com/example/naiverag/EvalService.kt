package com.example.naiverag
import com.example.ragcommon.EvalHarness
import org.springframework.stereotype.Service
@Service
class EvalService(private val retrievalService: RetrievalService,
                  private val promptBuilderService: PromptBuilderService,
                  private val generationService: GenerationService,
                  private val faithfulnessGuardService: FaithfulnessGuardService) {
    fun run() {
        println("\n=== Eval Loop ===")
        EvalHarness.pairs.forEachIndexed { idx, pair ->
            println("\n--- Q${idx + 1}: ${pair.q} ---")
            val chunks = retrievalService.retrieve(pair.q)
            val relevance = if (chunks.isNotEmpty()) 1.0 else 0.0
            val context = chunks.joinToString("\n")
            val answer = if (chunks.isEmpty()) {
                "I don't have enough information to answer that"
            } else {
                val prompt = promptBuilderService.build(pair.q, chunks)
                generationService.generate(prompt)
            }
            val faithful = if (chunks.isNotEmpty()) faithfulnessGuardService.check(answer, context) else false
            println("Answer: $answer")
            println("Relevance: $relevance | Faithful: $faithful")
        }
    }
}
