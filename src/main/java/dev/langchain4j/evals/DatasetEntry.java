package dev.langchain4j.evals;

import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

record DatasetEntry(String query, List<TextSegment> expectedContextResults, String answer) {

}
