package dev.langchain4j.evals.evaluators;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.evals.ROUGE.ROUGE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RougeMatchingChunkEvaluator implements RetrievalEvaluator{

    private static ROUGE rougeScorer;

    public RougeMatchingChunkEvaluator() {
        rougeScorer = new ROUGE(ROUGE.RougeTypes.ROUGEL);
    }

    private boolean match(String string1, String string2){
        double score = rougeScorer.calculate(string1, string2);
        return score >= 0.8;
    }

    @Override
    public Map<String, Double> evaluate(List<TextSegment> groundTruthDocuments, List<TextSegment> retrievedDocuments) {
        List<String> groundTruthChunks = groundTruthDocuments.stream().map(TextSegment::text).toList();
        List<String> retrievedChunks = retrievedDocuments.stream().map(TextSegment::text).toList();

        Set<String> hitGroundTruthChunks = new HashSet<>();

        for (String retrievedChunk : retrievedChunks) {
            for (String groundTruthChunk : groundTruthChunks) {
                if (match(groundTruthChunk, retrievedChunk)) {
                    hitGroundTruthChunks.add(retrievedChunk);
                    break;
                }
            }
        }

        double recall = !groundTruthChunks.isEmpty() ? 1 * (hitGroundTruthChunks.size() / (double) (groundTruthChunks.size())) : 0;
        return Map.of("Recall", recall);
    }
}
