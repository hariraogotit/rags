# rags — Multimodule RAG Curriculum

This is a multimodule Gradle project. Each module is an independent RAG pattern stage.

## Modules

- `rag-common/` — shared chunking, config, prompts, guards, eval harness. Has its own README.
- `rag-naive/` — naive dense retrieval (Chroma + OpenRouter). Has its own README.
- `rag-hybrid/` — hybrid BM25 (Lucene) + dense retrieval with RRF. Has its own README.

Each module runs independently (`gradlew :<module>:bootRun`) and references the shared library where appropriate.

## Root notes

- `test-data/` lives inside `rag-naive/` (test corpus for all modules).
- `lucene-index/` lives inside `rag-hybrid/` (BM25 index).
- Chroma runs locally at `localhost:8000`.
- `OPENROUTER_API_KEY` required as env var (never hardcoded).
