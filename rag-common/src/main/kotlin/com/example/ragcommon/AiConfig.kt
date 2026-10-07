package com.example.ragcommon

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Explicit ChatClient bean — why: we want generation (step 5)
 * to be independently callable and visible, not hidden inside an
 * all-in-one advisor. ChatClient is just the low-level prompt/
 * response layer; we still control retrieval and prompt construction
 * manually in RetrievalService / PromptBuilderService.
 */
@Configuration
class AiConfig {

    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient {
        return ChatClient.create(chatModel)
    }
}
