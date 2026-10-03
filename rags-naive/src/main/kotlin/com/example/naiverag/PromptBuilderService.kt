package com.example.naiverag
import com.example.ragcommon.PromptBuilder
import org.springframework.stereotype.Service
@Service
class PromptBuilderService {
    fun build(query: String, chunks: List<String>): String = PromptBuilder.build(query, chunks)
}
