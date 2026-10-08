# rag-naive — Naive RAG Pipeline

Manual, visible steps (no `QuestionAnswerAdvisor` / `RetrievalAugmentationAdvisor`).

```mermaid
flowchart TD
    A["test-data .md/.txt"] -->|chunk with split| B[Document chunks]
    B -->|Embed + store via VectorStore| C[(Chroma)]
    Q["User query"] -->|search request| D[RetrievalService.retrieve]
    D -->|guardrail| E{Results?}
    E -->|No| F["No info"]
    E -->|Yes| G[PromptBuilder.build]
    G -->|manual prompt| H[GenerationService.generate]
    H --> I[Answer]
    I -->|faithfulness check| J{Supported?}
    J -->|No| K[Lower-confidence response]
    J -->|Yes| L[Return answer]
    D --> M[Log retrieved chunks]
    M --> N[Eval loop 10 QA pairs]
    style A fill:#e8f0fe,stroke:#1565c0,color:#000
    style B fill:#fff3e0,stroke:#e65100,color:#000
    style C fill:#e8f5e9,stroke:#2e7d32,color:#000
    style D fill:#fce4ec,stroke:#880e4f,color:#000
    style E fill:#fff9c4,stroke:#f57f17,color:#000,stroke-width:2px,shape:diamond
    style F fill:#e0f7fa,stroke:#006064,color:#000
    style G fill:#fff8e1,stroke:#ffa000,color:#000
    style H fill:#e8f5e9,stroke:#2e7d32,color:#000
    style I fill:#ffebee,stroke:#c62828,color:#000
    style J fill:#fffde7,stroke:#f9a825,color:#000,shape:diamond
    style K fill:#fff3e0,stroke:#e65100,color:#000
    style L fill:#e8f5e9,stroke:#2e7d32,color:#000
    style M fill:#f3e5f5,stroke:#6a1b9a,color:#000
    style N fill:#f3e5f5,stroke:#6a1b9a,color:#000
```

## Key files
- `DocumentLoaderService` / `Chunker` — load & split
- `EmbeddingStoreService` — `vectorStore.add()`
- `RetrievalService` — `SearchRequest` with threshold guardrail
- `PromptBuilderService` — manual prompt construction
- `GenerationService` — direct `chatClient.prompt()`
- `FaithfulnessGuardService` — output guardrail
- `EvalService` — regression eval loop
- `PipelineInitializer` — startup load + store + eval
