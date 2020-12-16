package org.example;

import java.util.ArrayList;
import java.util.List;

public class ProgramAnalyzerResult {
    List<AnalyzerResult> analyzerResult = new ArrayList<AnalyzerResult>();
    List<Variable> variables = new ArrayList<Variable>();
    List<NumberBinary> numbers = new ArrayList<NumberBinary>();

    ProgramAnalyzerResult(List<AnalyzerResult> analyzerResult, List<Variable> variables, List<NumberBinary> numbers) {
        this.analyzerResult = analyzerResult;
        this.variables = variables;
        this.numbers = numbers;
    }

    ProgramAnalyzerResult(List<AnalyzerResult> analyzerResult, List<Variable> variables) {
        this.analyzerResult = analyzerResult;
        this.variables = variables;
    }

    ProgramAnalyzerResult(List<AnalyzerResult> analyzerResult) {
        this.analyzerResult = analyzerResult;
    }

    ProgramAnalyzerResult() {}

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public List<NumberBinary> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<NumberBinary> numbers) {
        this.numbers = numbers;
    }

    public List<AnalyzerResult> getAnalyzerResult() {
        return analyzerResult;
    }

    public void setAnalyzerResult(List<AnalyzerResult> analyzerResult) {
        this.analyzerResult = analyzerResult;
    }
}
