# evals-demo

Demo for the Devoxx presentation of the future evals capabilities of langchain4j

## Requirements
- Java 21+
- (optional for generation demo) access to one of the LLMs supported by langchain4j

## How to run
```shell
mvn clean install
mvn exec:java -Dexec.mainClass="dev.langchain4j.evals.PrecisionRecallEvaluationDemo"
```
or
```shell
mvn clean install
mvn exec:java -Dexec.mainClass="dev.langchain4j.evals.GenerationDemo"
```
for the generation demo

After that any aspect of the application can be changed. Some examples are
- chunk size and overlap
- max number of returned entries
- changing the embedding model

Be sure to mix and match to see how the result is affected

## Generation Demo notes
The generation demo uses JLama for the Bert based evaluator be sure to add `JDK_JAVA_OPTIONS=--add-modules jdk.incubator.vector --enable-preview`.
By default, an openai model is used so an OpenAi api key is needed but any other llm from langchain4j can be put in its place.