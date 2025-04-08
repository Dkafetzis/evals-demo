package dev.langchain4j.evals.evaluators;

import java.util.Map;

public interface GenerationEvaluator {
    public Map<String, Double> evaluate(String generatedText, String groundTruthText);
}
