package com.example.naiverag

import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

/**
 * Step 5: Generation.
 * We call chatClient.prompt() explicitly rather than using an advisor wrapper,
 * so the prompt construction step above remains separate and inspectable.
 */
@Service
class GenerationService(private val chatClient: ChatClient) {

    fun generate(promptText: String): String {
        return chatClient.prompt().user(promptText).call().content() ?: "No response."
    }
}
