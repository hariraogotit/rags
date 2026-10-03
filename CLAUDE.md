# RAG Pattern Curriculum — Kotlin + Spring AI + Chroma + OpenRouter

## Project overview

This is a **multi-module Gradle project**, one module per RAG pattern, built as a
hands-on learning curriculum (naive → hybrid search → reranking → query rewriting →
agentic → GraphRAG). Each module should be independently runnable so earlier patterns
stay as working reference points when evaluating later ones.

Modules:
- `rag-common` — **DO THIS FIRST**, before any `rag-hybrid` retrieval code. Shared
  library extracted from `rag-naive`. Described below.
- `rag-naive` — **COMPLETE**, but will be refactored in this session to depend on
  `rag-common` (see Module: rag-common below). Its retrieval logic (plain similarity
  search) stays in this module.
- `rag-hybrid` — **CURRENT PATTERN MODULE**. Described in full below. Depends on
  `rag-common`.

Ecosystem decision for the whole curriculum: **Kotlin/Spring AI is sufficient for
every pattern through reranking.** For hybrid search, use **Apache Lucene** (pure JVM,
embedded, no server) for BM25 — not Elasticsearch. Elasticsearch is Lucene plus a
server process, clustering, and a REST layer; none of that is needed to BM25-search a
local test corpus, so it would only add a service to run and manage for no benefit at
this stage. (Worth revisiting later only if we explicitly want to mirror a
production-style deployment — not now.) For reranking (the next module after
`rag-hybrid`), OpenRouter has a native rerank endpoint (`POST /api/v1/rerank`, e.g.
`cohere/rerank-v3.5`) reachable via plain HTTP from Kotlin — also no Python needed.
Agentic patterns later may use **Embabel** (JVM-native agentic framework) as a
LangGraph alternative — not needed yet, flag this when we reach that module.

## Shared configuration (same across all modules)

- `base-url: https://openrouter.ai/api/v1`, `api-key: ${OPENROUTER_API_KEY}` (env var,
  never hardcoded/committed)
- Chat model: `openrouter/free` (random free model per request — **not reproducible
  run-to-run**, which matters for eval comparisons; keep this in mind when comparing
  module eval scores against each other)
- Embedding model: `nvidia/nemotron-3-embed-1b:free`
- Chroma running locally at `localhost:8000` (already running — don't add setup
  instructions for it)
- Guardrail pattern (apply in every module): similarity/relevance threshold before
  generation, faithfulness check after generation. Never pass empty/irrelevant context
  to the LLM silently.
- Eval pattern (apply in every module): reuse the **same** 10–15 question/answer pairs
  from `rag-naive`'s eval set so results are comparable module-to-module. Record
  faithfulness/relevance scores per module so we can see whether each added pattern
  actually improved results on this corpus — don't assume it will.

---

## Module: `rag-common` — shared library (build this first)

### Goal

Extract the code that is identical across every RAG pattern module into one shared
module, so `rag-naive`, `rag-hybrid`, and future pattern modules depend on it instead
of duplicating it. Only the parts of the pipeline that differ *by pattern* stay out of
`rag-common` — chiefly, retrieval logic itself.

### What belongs in `rag-common`

- **Document loading**: reading `.md`/`.txt` files from the test-data folder into
  Spring AI `Document` objects.
- **Chunking**: the `TokenTextSplitter` call with the agreed settings (~500 tokens,
  ~50 overlap). Keep these settings in one place so every module chunks identically —
  this matters for `rag-hybrid`, where Lucene's index and Chroma's vector store must be
  built from identical chunk IDs.
- **Spring AI configuration**: the `EmbeddingModel`/`ChatModel`/`VectorStore` bean
  wiring pointed at OpenRouter + Chroma.
- **Prompt construction helper**: the "answer using only this context" template
  function, parameterized by retrieved context + query.
- **Guardrail functions**: the similarity/relevance threshold check and the
  faithfulness check, as standalone, testable functions.
- **Eval harness**: loading the shared Q&A pairs, running them through a given
  retrieval function (passed in as a parameter/lambda so each module can plug in its
  own retrieval), and scoring/logging results.

### What stays OUT of `rag-common`

- Retrieval logic itself — `rag-naive`'s plain similarity search, `rag-hybrid`'s
  BM25+RRF fusion, and later modules' reranking/query-rewriting logic. This is the
  part that's actually different per pattern; abstracting it away would defeat the
  point of the curriculum.

### Build plan — implement in this order

1. Create the `rag-common` Gradle module (add to `settings.gradle.kts`), with its own
   `build.gradle.kts` depending on the Spring AI starters (`spring-ai-starter-model-openai`,
   `spring-ai-starter-vector-store-chroma`) but NOT Lucene (that's `rag-hybrid`-specific).
2. Move the pieces listed above out of `rag-naive` into `rag-common`, as clean public
   functions/classes.
3. Update `rag-naive`'s `build.gradle.kts` to add `implementation(project(":rag-common"))`,
   and update its code to call the shared functions instead of the now-removed
   duplicated versions.
4. **Regression check**: re-run `rag-naive`'s existing eval suite after the refactor and
   confirm the faithfulness/relevance scores match what they were before the refactor.
   This is a pure refactor — behavior must not change. If scores differ, the refactor
   introduced a bug; fix before moving on to `rag-hybrid`.

---

## Module: `rag-hybrid` — Hybrid Search (BM25 + Dense Retrieval via RRF)

### Goal

Add lexical (keyword) search alongside the existing dense (embedding) search, fuse the
two ranked lists with Reciprocal Rank Fusion (RRF), and compare eval results against
the naive baseline. This catches exact-term matches (IDs, config keys, specific
phrases) that embedding similarity alone tends to miss.

**Do not add reranking in this module** — that's the next module after this one. Keep
this module scoped to hybrid retrieval only. Depends on `rag-common` (built above) for
chunking, config, prompt construction, guardrails, and eval.

### Additional dependencies (on top of `rag-common`'s)

```kotlin
dependencies {
    implementation(project(":rag-common"))
    implementation("org.apache.lucene:lucene-core:9.11.1")
    implementation("org.apache.lucene:lucene-analysis-common:9.11.1")
    implementation("org.apache.lucene:lucene-queryparser:9.11.1")
}
```

### Build plan — implement in this order

1. **Chunk via `rag-common`**: use the shared chunking function so chunk IDs match
   exactly what gets embedded into Chroma — this is required for RRF in step 4 to work.

2. **Build a Lucene BM25 index**: Lucene is a library, not a server — unlike Chroma, it
   has no storage of its own, so the index location must be chosen explicitly. Use
   `FSDirectory.open(Paths.get("./lucene-index"))` (on-disk, persists across runs,
   mirrors how `rag-naive` persists Chroma to `./chroma-data`) — NOT
   `ByteBuffersDirectory` (in-memory, rebuilt every run, would make the index
   disappear between app restarts). Create an `IndexWriter` over that `FSDirectory`
   (default `Similarity` is already BM25 as of Lucene 6+, no config needed). For each
   chunk, index a `chunkId` field (`StringField`, matching the ID used in Chroma) and a
   `content` field (`TextField`, analyzed with `StandardAnalyzer`). Commit and close the
   writer once.

3. **Query both retrievers in parallel for a given user query**:
   - Dense: `vectorStore.similaritySearch(...)` (via `rag-common`'s configured
     `VectorStore` bean), get ranked chunk IDs.
   - Lexical: `QueryParser("content", StandardAnalyzer()).parse(query)` against the
     Lucene index via `IndexSearcher`, get ranked chunk IDs (top ~10–20 from each side
     is enough headroom for fusion).

4. **Fuse with Reciprocal Rank Fusion (RRF)**: For each chunk ID, compute
   `score = 1 / (k + rank)` per list it appears in (use `k = 60`, the standard RRF
   constant), sum across both lists (a chunk appearing in both lists scores higher than
   one appearing in only one), sort descending, take the fused top-k (e.g. top 5) to
   pass downstream. Implement this as a small standalone, testable function in this
   module (it's the one genuinely new piece of logic here) — easy to unit test
   independently of Lucene/Chroma.

5. **Guardrail, prompt, generate, output-check**: Use `rag-common`'s guardrail and
   prompt-construction functions on the fused results — don't reintroduce an advisor
   here either.

6. **Eval and compare**: Run `rag-common`'s eval harness, passing this module's
   fused-retrieval function in. Log faithfulness/relevance scores side-by-side with
   `rag-naive`'s scores. The goal of this module isn't just "hybrid search works" —
   it's **quantifying whether it actually helped on this corpus**, since published
   benchmarks disagree on how much hybrid search helps depending on the domain.

### Code style

Same as `rag-naive`: idiomatic Kotlin, constructor injection, small named/testable
functions (especially the RRF fusion function), comments explaining *why*, no
all-in-one retrieval abstractions.

### Out of scope for this module

Reranking, query rewriting/HyDE, multi-hop retrieval, agentic retrieval, and GraphRAG
are later modules — do not add them now.
