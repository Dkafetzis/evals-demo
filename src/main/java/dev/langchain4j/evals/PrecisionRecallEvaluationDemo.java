package dev.langchain4j.evals;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.evals.evaluators.FuzzyMatchingChunkEvaluator;
import dev.langchain4j.evals.evaluators.RougeMatchingChunkEvaluator;
import dev.langchain4j.evals.evaluators.SentenceMatchingEvaluator;
import dev.langchain4j.evals.evaluators.TokenMatchingEvaluator;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.HashMap;
import java.util.List;


public class PrecisionRecallEvaluationDemo {
    public static void main(String[] args) {

        int CHUNK_SIZE = 1000;
        int OVERLAP_SIZE = 0;
        int MAX_RESULTS = 5;

        //Load all documents from lc4j documentation.
        List<Document> l4jDocuments = FileSystemDocumentLoader.loadDocumentsRecursively("./langchain4j-docs");

        //Split the documents.
        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, OVERLAP_SIZE);
        List<TextSegment> segments = splitter.splitAll(l4jDocuments);

        //Calculate embeddings for them.
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();


        //Add them to embedding store.
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        var fuzzyMatchingEvaluator = new FuzzyMatchingChunkEvaluator();

        HashMap<String, Double> averageFuzzyResults = new HashMap<>();

        for (DatasetEntry entry: Dataset.get()){
            var queryEmbedding = embeddingModel.embed(entry.query()).content();
            var searchRequest = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).maxResults(MAX_RESULTS).build();
            var searchResult = embeddingStore.search(searchRequest);


            var fuzzyResults = fuzzyMatchingEvaluator.evaluate(entry.expectedContextResults(), searchResult.matches().stream().map(EmbeddingMatch::embedded).toList());
            for (String key: fuzzyResults.keySet()){
                averageFuzzyResults.put(key, averageFuzzyResults.getOrDefault(key, 0.0) + fuzzyResults.get(key));
            }

        }

        System.out.println("Average fuzzy results:");
        for (String key: averageFuzzyResults.keySet()){
            System.out.println(key + ": " + averageFuzzyResults.get(key) / Dataset.get().size());
        }
    }
}
