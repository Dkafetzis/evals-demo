package dev.langchain4j.evals.ROUGE;

import dev.langchain4j.evals.ROUGE.utils.F1Score;
import dev.langchain4j.evals.ROUGE.utils.NGramUtils;
import dev.langchain4j.evals.ROUGE.utils.TokenizerUtils;
import opennlp.tools.tokenize.Tokenizer;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

//Adapted for now from https://github.com/trustyai-explainability/trustyai-explainability/blob/main/explainability-core/src/main/java/org/kie/trustyai/metrics/language/rouge/ROUGE.java
public class ROUGE {
    private final Tokenizer tokenizer;
    private final RougeTypes rougeType;


    public ROUGE(RougeTypes rougeType) {
        this.tokenizer = TokenizerUtils.getDefaultTokenizer();
        this.rougeType = rougeType;
    }

    public enum RougeTypes {
        ROUGE1,
        ROUGE2,
        ROUGEL;

        public int getN() {
            switch (this) {
                case ROUGE1:
                    return 1;
                case ROUGE2:
                    return 2;
                default:
                    throw new UnsupportedOperationException("getN() is not supported for " + this);
            }
        }
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }


    public Double calculate(String reference, String hypothesis) {
        return calculate(reference, hypothesis, rougeType);
    }

    public Double calculatePrecision(String reference, String hypothesis) {
        final List<String> referenceTokens = Arrays.asList(this.getTokenizer().tokenize(reference));
        final List<String> hypothesisTokens = Arrays.asList(this.getTokenizer().tokenize(hypothesis));
        return rougeL(referenceTokens, hypothesisTokens).get("P");
    }

    public Double calculateRecall(String reference, String hypothesis) {
        final List<String> referenceTokens = Arrays.asList(this.getTokenizer().tokenize(reference));
        final List<String> hypothesisTokens = Arrays.asList(this.getTokenizer().tokenize(hypothesis));
        return rougeL(referenceTokens, hypothesisTokens).get("R");
    }

    public Double calculateF1(String reference, String hypothesis) {
        final List<String> referenceTokens = Arrays.asList(this.getTokenizer().tokenize(reference));
        final List<String> hypothesisTokens = Arrays.asList(this.getTokenizer().tokenize(hypothesis));
        return rougeL(referenceTokens, hypothesisTokens).get("F1");
    }

    /**
     * Calculates the ROUGE score for a reference and a hypothesis.
     *
     * @param reference The reference string.
     * @param hypothesis The hypothesis string.
     * @param rougeType The ROUGE type string.
     * @return The ROUGE score.
     */
    public Double calculate(String reference, String hypothesis, RougeTypes rougeType) {
        switch (rougeType) {
            case ROUGEL:
            case ROUGE1:
            case ROUGE2:
                // Remove punctuation from the reference and hypothesis
                reference = reference.replaceAll("\\p{Punct}", "");
                hypothesis = hypothesis.replaceAll("\\p{Punct}", "");
                // Tokenize the reference and hypothesis
                final List<String> referenceTokens = Arrays.asList(this.getTokenizer().tokenize(reference));
                final List<String> hypothesisTokens = Arrays.asList(this.getTokenizer().tokenize(hypothesis));
                if (rougeType != rougeType.ROUGEL) {
                    // Get the ROUGE-N type and calculate the n-grams in the reference and hypothesis accordingly
                    final int n = rougeType.getN();
                    final Map<String, Integer> referenceNgramCount = NGramUtils.countNgrams(referenceTokens, n);
                    final Map<String, Integer> hypothesisNGramCount = NGramUtils.countNgrams(hypothesisTokens, n);
                    return rougeN(referenceNgramCount, hypothesisNGramCount);
                }
                return rougeL(referenceTokens, hypothesisTokens).get("R");
            default:
                return 0.0;
        }
    }

    /**
     * Calculate ROUGE-N score given the reference and hypothesis n-gram count
     *
     * @param referenceNgramCount N-grams for reference
     * @param hypothesisNgramCount N-grams for hypothesis
     * @return ROUGE-N score
     */
    public Double rougeN(Map<String, Integer> referenceNgramCount, Map<String, Integer> hypothesisNgramCount) {
        // Initalize total match count between reference and hypothesis tokens
        int totalMatchCount = 0;

        // Get length of reference and hypothesis tokens
        int referenceTokensLength = referenceNgramCount.values().stream().mapToInt(Integer::intValue).sum();
        int hypothesisTokensLength = hypothesisNgramCount.values().stream().mapToInt(Integer::intValue).sum();
        ;

        // Count matches between reference and hypothesis tokens
        for (Map.Entry<String, Integer> entry : hypothesisNgramCount.entrySet()) {
            final String ngram = entry.getKey();
            final int count = entry.getValue();
            totalMatchCount += Math.min(count, referenceNgramCount.getOrDefault(ngram, 0));
        }

        // Calculate precision and recall scores
        final Double rawPrecisionScore = (double) totalMatchCount / referenceTokensLength;
        final Double rawRecallScore = (double) totalMatchCount / hypothesisTokensLength;

        return F1Score.calculate(rawPrecisionScore, rawRecallScore);
    }

    /**
     * Calculate ROUGE-L score given the reference and hypothesis tokens
     *
     * @param referenceTokens Reference tokens
     * @param hypothesisTokens Hypothesis tokens
     * @return ROUGE-L score
     */
    public Map<String, Double> rougeL(List<String> referenceTokens, List<String> hypothesisTokens) {
        // Initialize matrix to store the number of consecutive token matches between
        // the reference and hypothesis
        int rows = referenceTokens.size();
        int cols = hypothesisTokens.size();
        int[][] lcsTable = new int[rows + 1][cols + 1];

        // Iterate through each reference-hypothesis token pair and add 1 to the previous
        // value of the diagonal if they are equal
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                if (referenceTokens.get(i - 1).equals(hypothesisTokens.get(j - 1))) {
                    lcsTable[i][j] = lcsTable[i - 1][j - 1] + 1;
                } else {
                    lcsTable[i][j] = Math.max(lcsTable[i - 1][j], lcsTable[i][j - 1]);
                }
            }
        }
        // Get the total number of reference-hypothesis matches
        int lcsLength = lcsTable[rows][cols];
        double rawPrecisionScore = (double) lcsLength / cols;
        double rawRecallScore = (double) lcsLength / rows;
        double f1Score = F1Score.calculate(rawPrecisionScore, rawRecallScore);
        return Map.of("P", rawPrecisionScore, "R", rawRecallScore, "F1", f1Score);
    }
}
