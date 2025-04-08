package dev.langchain4j.evals.evaluators;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.jlama.JlamaEmbeddingModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BertGenerationEvaluator implements GenerationEvaluator {

    EmbeddingModel embeddingModel;

    public BertGenerationEvaluator() {
        embeddingModel = JlamaEmbeddingModel.builder()
                .modelName("sentence-transformers/all-MiniLM-L6-v2")
                .build();
    }

    private static double cosineSimilarity(List<Float> embedding1, List<Float> embedding2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += Math.pow(embedding1.get(i), 2);
            norm2 += Math.pow(embedding2.get(i), 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private List<Float> calculateEmbedding(String text) {
        try {
            return embeddingModel.embed(text).content().vectorAsList();
        } catch (IllegalArgumentException e){
            DocumentSplitter splitter = new DocumentBySentenceSplitter(384, 0);
            Document doc = new Document(text);
            List<TextSegment> res = splitter.split(doc);
            List<Float> embedding = new ArrayList<>(Collections.nCopies(384, 0.0f));
            for (TextSegment sentence : res){
                List<Float> sentenceEmbedding = embeddingModel.embed(sentence.text()).content().vectorAsList();
                for (int i = 0; i < embedding.size(); i++) {
                    embedding.set(i, embedding.get(i) + sentenceEmbedding.get(i));
                }
            }
            return embedding;
        }
    }

    @Override
    public Map<String, Double> evaluate(String generatedText, String groundTruthText) {

        double calculatedSimilarity = cosineSimilarity(
                calculateEmbedding(generatedText),
                calculateEmbedding(groundTruthText));
        return Map.of("Cosine Similarity", calculatedSimilarity);
    }
}
