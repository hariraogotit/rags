# rag-naive — Naive RAG Pipeline

Manual, visible steps (no `QuestionAnswerAdvisor` / `RetrievalAugmentationAdvisor`).

```mermaid
flowchart TD
    A["test-data .md/.txt"] -->|chunk with split| B[Document chunks]
    B -->|Embed + store via VectorStore| C[(Chroma)]
    Q["User query"] -->|search request| D[RetrievalService.retrieve]
    D -->|guardrail empty| E{Results?}
    E -->|No| F["No info"]
    E -->|Yes| G[PromptBuilder.build]
    G -->|manual prompt| H[GenerationService.generate]
    H --> I[Answer]
    I -->|fidelity check| J{Supported?}
    J -->|No| K[Lower-confidence response]
    J -->|Yes| L[Return answer]
    D --> M[Log retrieved chunks]
    M --> N[Eval loop 10 QA pairs]
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
