package com.example.raghybrid
import com.example.ragcommon.PromptBuilder
import com.example.ragcommon.FaithfulnessGuard
import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HybridController(private val hybrid: HybridRetrieval, private val chatClient: ChatClient) {
    @GetMapping("/hybrid")
    fun hybrid(@RequestParam q: String): Map<String, Any> {
        val chunks = hybrid.retrieve(q)
        val answer = if (chunks.isEmpty()) "I don't have enough information to answer that"
        else {
            val prompt = PromptBuilder.build(q, chunks)
            chatClient.prompt().user(prompt).call().content() ?: "No response"
        }
        val faithful = if (chunks.isNotEmpty()) FaithfulnessGuard.check(answer, chunks.joinToString("\n"), chatClient) else false
        return mapOf("query" to q, "retrieved" to chunks.size, "chunkIds" to chunks, "answer" to answer, "faithful" to faithful)
    }
}
