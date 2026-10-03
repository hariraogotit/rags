package com.example.raghybrid
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

class HybridRetrieval {
    private val dir = FSDirectory.open(Paths.get("./lucene-index"))
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

    fun query(query: String): List<String> {
        val parser = QueryParser("content", analyzer)
        val q = parser.parse(query)
        val reader = org.apache.lucene.index.DirectoryReader.open(dir)
        val searcher = IndexSearcher(reader)
        val top = searcher.search(q, 10)
        return top.scoreDocs.map { searcher.doc(it.doc).get("chunkId") }.filterNotNull()
    }
}
