package dev.langchain4j.evals;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.evals.evaluators.BertGenerationEvaluator;
import dev.langchain4j.evals.evaluators.RougeLSimilarityEvaluator;
import dev.langchain4j.model.openai.OpenAiChatModel;

//JDK_JAVA_OPTIONS=--add-modules jdk.incubator.vector --enable-preview needs to be enabled in order to use the BERT evaluator
public class GenerationDemo {
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .build();

        double averageRougeScore = 0;
        double averageBertScore = 0;

        for (DatasetEntry entry : Dataset.get()) {
            StringBuilder context = new StringBuilder();
            for (TextSegment textSegment : entry.expectedContextResults()) {
                context.append(textSegment.text()).append("\n");
            }
            String generatedAnswer = model.generate("Given this context:\n\n"+context+"\n Answer this question:\n"+ entry.query());
            System.out.println("---------------------------------------");
            RougeLSimilarityEvaluator ase = new RougeLSimilarityEvaluator();
            BertGenerationEvaluator be = new BertGenerationEvaluator();
            System.out.println("Ground truth: "+entry.answer());
            System.out.println("Generated answer: "+generatedAnswer);
            double rougeScore = ase.evaluate(entry.answer(), generatedAnswer).get("AnswerScore");
            double bertScore = be.evaluate(entry.answer(), generatedAnswer).get("Cosine Similarity");
            System.out.println("Rouge Similarity: " + rougeScore);
            System.out.println("Bert Similarity: " + bertScore);
            averageRougeScore += rougeScore;
            averageBertScore += bertScore;
        }

        System.out.println("Average Rouge Similarity: " + averageRougeScore/Dataset.get().size());
        System.out.println("Average Bert Similarity: " + averageBertScore/Dataset.get().size());
    }
}
