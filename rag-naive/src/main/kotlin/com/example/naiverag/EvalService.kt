package com.example.naiverag
import com.example.ragcommon.EvalHarness
import com.example.ragcommon.FaithfulnessGuard
import com.example.ragcommon.PromptBuilder
import org.springframework.stereotype.Service
@Service
class EvalService(private val retrievalService: RetrievalService,
                  private val generationService: GenerationService,
                  private val chatClient: org.springframework.ai.chat.client.ChatClient) {
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
                val prompt = PromptBuilder.build(pair.q, chunks)
                generationService.generate(prompt)
            }
            val faithful = if (chunks.isNotEmpty()) FaithfulnessGuard.check(answer, context, chatClient) else false
            println("Answer: $answer")
            println("Relevance: $relevance | Faithful: $faithful")
        }
    }
}
