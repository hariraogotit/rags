package com.example.naiverag
import com.example.ragcommon.Chunker
import org.springframework.ai.document.Document
import org.springframework.stereotype.Service
import java.nio.file.Path
@Service
class DocumentLoaderService {
    fun loadAndChunk(directory: Path): List<Document> = Chunker.loadAndChunk(directory)
}
