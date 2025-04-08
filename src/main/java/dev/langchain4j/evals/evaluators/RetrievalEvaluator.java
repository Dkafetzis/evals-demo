package dev.langchain4j.evals.evaluators;

import dev.langchain4j.data.segment.TextSegment;

import java.util.List;
import java.util.Map;

public interface RetrievalEvaluator {

    public Map<String, Double> evaluate(List<TextSegment> groundTruthDocuments, List<TextSegment> retrievedDocuments);
}
