package com.example.naiverag
import com.example.ragcommon.FaithfulnessGuard
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
@Service
class FaithfulnessGuardService(private val chatClient: ChatClient) {
    fun check(answer: String, context: String): Boolean = FaithfulnessGuard.check(answer, context, chatClient)
}
