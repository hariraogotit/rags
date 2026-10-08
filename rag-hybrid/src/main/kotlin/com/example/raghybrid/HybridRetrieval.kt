package com.example.raghybrid
import com.example.ragcommon.Chunker
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
class HybridRetrieval(private val vectorStore: VectorStore) {
    private val dir = FSDirectory.open(Paths.get("rag-hybrid/lucene-index"))
    private val analyzer = StandardAnalyzer()

    fun buildIndex(chunks: List<String>) {
        val cfg = IndexWriterConfig(analyzer)
        IndexWriter(dir, cfg).use { w ->
            chunks.forEachIndexed { i, text ->
                val d = org.apache.lucene.document.Document()
                d.add(org.apache.lucene.document.StringField("chunkId", i.toString(), org.apache.lucene.document.Field.Store.YES))
                d.add(org.apache.lucene.document.TextField("content", text, org.apache.lucene.document.Field.Store.NO))
                w.addDocument(d)
            }
            w.commit()
        }
    }

    fun retrieve(query: String): List<String> {
        // Dense (similarity search)
        val denseReq = SearchRequest.builder().query(query).topK(5).similarityThreshold(0.7).build()
        val denseIds = vectorStore.similaritySearch(denseReq).map { it.id ?: it.text ?: "" }
        // Lexical (keyword search)
        val parser = QueryParser("content", analyzer)
        val q = parser.parse(query)
        val reader = org.apache.lucene.index.DirectoryReader.open(dir)
        val searcher = IndexSearcher(reader)
        // Search top 10 keyword matches and get their chunk IDs
        val hits = searcher.search(q, 10).scoreDocs.map { searcher.doc(it.doc).get("chunkId") }.filterNotNull()
        // RRF: reciprocal rank fusion (k=60)
        val rrf = mutableMapOf<String, Double>()
        denseIds.forEachIndexed { i, id -> rrf[id] = rrf.getOrDefault(id, 0.0) + 1.0 / (60 + i + 1) }
        hits.forEachIndexed { i, id -> rrf[id] = rrf.getOrDefault(id, 0.0) + 1.0 / (60 + i + 1) }
        return rrf.toList().sortedByDescending { it.second }.map { it.first }
    }
}
