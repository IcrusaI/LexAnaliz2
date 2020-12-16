package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {
    @FXML
    // Ключевые слова (1)
    private ListView<String> keywordsList;

    @FXML
    // Разделители (2)
    private ListView<String> separatorsList;

    @FXML
    // Идентификаторы (3)
    private ListView<String> variablesList;

    @FXML
    // Числа (4)
    private ListView<String> numbersList;

    @FXML
    // Исходный код
    private TextArea code;

    @FXML
    // Результат анализа
    private TextArea analyzerResult;

    @FXML
    // Кнопка "Проверить"
    private Button checkButton;

    @FXML
    private void validateProgram() {
        ProgramAnalyzer analyzer = new ProgramAnalyzer(code.getText());

        try {
            ProgramAnalyzerResult result = analyzer.scan();

            setVariablesList(result.variables);
            setNumbersList(result.numbers);
            setAnalyzerResult(result.analyzerResult);
        } catch (Exception err) {
            setAnalyzerResult(err);
        }
    }

    private void setKeywordsList(List<String> keywords) {
        ObservableList<String> data = FXCollections.observableArrayList();

        for (int i = 0; i < keywords.size(); i++) {
            Number key = i + 1;
            String value = keywords.get(i);

            String result = String.format("%s. %s", key, value);

            data.add(result);
        }

        keywordsList.setItems(data);
    }

    private void setSeparatorsList(List<String> separators) {
        ObservableList<String> data = FXCollections.observableArrayList();

        for (int i = 0; i < separators.size(); i++) {
            Number key = i + 1;
            String value = separators.get(i);

            String result = String.format("%s. %s", key, value);

            data.add(result);
        }

        separatorsList.setItems(data);
    }

    private void setVariablesList(List<Variable> variables) {
        ObservableList<String> data = FXCollections.observableArrayList();

        for (int i = 0; i < variables.size(); i++) {
            Number key = i + 1;
            String name = variables.get(i).name;
            String type = variables.get(i).type;

            String result = String.format("%s. %s: %s", key, name, type);

            data.add(result);
        }

        variablesList.setItems(data);
    }

    private void setNumbersList(List<NumberBinary> numbers) {
        ObservableList<String> data = FXCollections.observableArrayList();

        for (int i = 0; i < numbers.size(); i++) {
            Number key = i + 1;
            String original = numbers.get(i).original;
            String binary = numbers.get(i).getBinary();

            String result = String.format("%s. %s: %s", key, original, binary);

            data.add(result);
        }

        numbersList.setItems(data);
    }

    private void setAnalyzerResult(List<AnalyzerResult> analysis) {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < analysis.size(); i++) {
            AnalyzerResult e = analysis.get(i);
            String text = String.format("(%s, %s)", e.tableId, e.elementId);

            data.append(text);
            if (analysis.size() - i > 1) {
                data.append(", ");
            } else {
                data.append(".");
            }
        }

        analyzerResult.setText(data.toString());
    }

    public void setAnalyzerResult(Exception error) {
        analyzerResult.setText(error.getMessage());
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setKeywordsList(ProgramAnalyzer.keywords);
        setSeparatorsList(ProgramAnalyzer.separators);
    }
}
