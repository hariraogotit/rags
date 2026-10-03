# Retrieval-Augmented Generation Overview

RAG combines retrieval of external knowledge with generation by an LLM. Instead of relying solely on parametric knowledge, the system fetches relevant documents and injects them into the prompt.

## Key Concepts

- Chunking: splitting long documents into small pieces (~500 tokens) with overlap (~50 tokens) so boundaries do not lose context.
- Embedding: converting text chunks into dense vectors using a model like nq-embed-v1 or nemotron-3-embed-1b.
- Vector store: indexing embeddings in a database like Chroma for similarity search.
- Similarity threshold: filtering retrieved results by a minimum score (e.g., 0.7) to avoid passing irrelevant context.
- Faithfulness check: verifying that the answer is supported by the retrieved context rather than inventing facts.

## Naive Pipeline

The naive pipeline is: load -> chunk -> embed/store -> retrieve -> prompt -> generate -> guardrail -> eval.
