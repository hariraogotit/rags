# rag-hybrid — Hybrid Search (BM25 + Dense via RRF)

## Diagram (Mermaid)

```mermaid
flowchart TD
    A[".md / .txt (test-data)"] --> B[rag-common Chunker]
    B --> C[(Chroma Dense)]
    B --> D[(Lucene FSDirectory BM25)]
    C --> E{RRF Fusion}
    D --> E
    E --> F[HybridRetrieval.retrieve]
    F --> G[/hybrid endpoint/]
    G --> H[PromptBuilder + ChatClient + FaithfulnessGuard]

    style A fill:#f9f,stroke:#333
    style B fill:#bbf,stroke:#333
    style E fill:#ffd1dc,stroke:#333,stroke-width:2px,color:#333,font-weight:bold,shape:diamond
    style F fill:#bbf,stroke:#333
    style G fill:#bfb,stroke:#333
    style H fill:#bfb,stroke:#333
    classDef whiteText fill:#666,color:#fff
    class A,B,F,G,H whiteText
```


## Flow (steps 1-6 from build plan)

1. **Chunk** via `rag-common` (`Chunker.loadAndChunk`).
2. **Dense**: `vectorStore.similaritySearch` → Chroma.
3. **Lexical**: `QueryParser` → Lucene `FSDirectory` (`rag-hybrid/lucene-index`).
4. **RRF**: `HybridRetrieval.retrieve()` fuses ranked lists.
5. **Eval**: `HybridEval` runs `EvalHarness.pairs`.
6. **Generate/Guard**: `/hybrid` uses `rag-common` `PromptBuilder` + `FaithfulnessGuard`.
