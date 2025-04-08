package dev.langchain4j.evals.evaluators;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.evals.ROUGE.utils.TokenizerUtils;
import opennlp.tools.tokenize.Tokenizer;


import java.util.*;

public class TokenMatchingEvaluator implements RetrievalEvaluator {

    private static Tokenizer tokenizer;

    public TokenMatchingEvaluator() {
        tokenizer = TokenizerUtils.getCommonsTokenizer();
    }

    @Override
    public Map<String, Double> evaluate(List<TextSegment> groundTruthDocuments, List<TextSegment> retrievedDocuments) {

        Set<String> groundTruthTokens = new HashSet<>();
        Set<String> retrievedTokens = new HashSet<>();

        assert tokenizer != null;
        for (TextSegment document : groundTruthDocuments){
            groundTruthTokens.addAll(List.of(tokenizer.tokenize(document.text())));
        }

        for (TextSegment retrievedDocument : retrievedDocuments){
            retrievedTokens.addAll(List.of(tokenizer.tokenize(retrievedDocument.text())));
        }

        Set<String> commonTokens = new HashSet<>();

        for (String groundTruthToken : groundTruthTokens) {
            for (String retrievedToken : retrievedTokens) {
                if (groundTruthToken.equals(retrievedToken)) {
                    commonTokens.add(groundTruthToken);
                }
            }
        }

        double precision = !retrievedTokens.isEmpty() ? 1 * (commonTokens.size() / (double) (retrievedTokens.size())) : 0;
        double recall = !groundTruthTokens.isEmpty() ? 1 * (commonTokens.size() / (double) (groundTruthTokens.size())) : 0;

        return Map.of("Precision", precision, "Recall", recall);
    }
}
