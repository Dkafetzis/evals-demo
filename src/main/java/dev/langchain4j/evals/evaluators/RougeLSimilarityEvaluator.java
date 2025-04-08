package dev.langchain4j.evals.evaluators;

import dev.langchain4j.evals.ROUGE.ROUGE;

import java.util.Map;

public class RougeLSimilarityEvaluator implements GenerationEvaluator {
    public Map<String, Double> evaluate(String groundTruthAnswer, String retrievedAnswer) {
        ROUGE rougeScorer = new ROUGE(ROUGE.RougeTypes.ROUGEL);
        double score = rougeScorer.calculatePrecision(groundTruthAnswer, retrievedAnswer);
        return Map.of("AnswerScore", score);
    }
}
