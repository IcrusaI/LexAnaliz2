package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgramAnalyzer {
    private static final String variableRegex = "(?![0-9])[0-9A-Za-z]+";
    private static final List<String> mathematicalOperators = Arrays.asList("+", "-", "*", "/");
    public static List<String> keywords = Arrays.asList("begin", "end", "int", "float", "bool", "if", "else", "for", "to", "step", "next", "while", "readln", "writeln", "true", "false");
    public static List<String> separators = Arrays.asList("!=", "==", "<", "<=", ">", ">=", "+", "-", "||", "*", "/", "&&", "!", "(", ")", ";", ",", ":", "=", "%");
    public static List<String> variableTypes = Arrays.asList("int", "float", "bool");
    public List<Variable> variables = new ArrayList<Variable>();
    public List<NumberBinary> numbers = new ArrayList<NumberBinary>();
    public List<AnalyzerResult> result = new ArrayList<AnalyzerResult>();
    private final List<String> code;
    private ListIterator<String> iterator;

    ProgramAnalyzer(String data) {
        // Разбиваем код построчно и записываем его в List code
        code = Arrays.asList(data.split("\n"));
    }

    public ProgramAnalyzerResult scan() throws Exception {
        iterator = code.listIterator();

        ProgramAnalyzerResult result = new ProgramAnalyzerResult();

        skipLineTranslation();

        // Инициализация программы
        if (!getCurrentString().equals("begin")) {
            throw new Exception("Программа должна начинаться с begin");
        }
        addToResult(1, "begin");

        initializationVariables();
        result.variables = variables;

        skipLineTranslation();

        if (!getCurrentString().equals("\tbegin")) {
            throw new Exception("Программа должна начинаться с begin");
        }
        addToResult(1, "begin");
        parseBody(2);

        result.numbers = numbers;
        result.analyzerResult = this.result;

        if (!parseString(getCurrentString(), 1).equals("end")) {
            throw new Exception("Программа должна закачиваться end.");
        } else {
            addToResult(1, "end");
        }

        if (!parseString(getCurrentString(), 0).equals("end")) {
            throw new Exception("Программа должна закачиваться end.");
        } else {
            addToResult(1, "end");
        }


        return result;
    }

    // Анализируем тело
    private void parseBody(Integer indent) throws Exception {
        while (iterator.hasNext()) {
            skipLineTranslation();

            String data = parseString(getCurrentString(), indent);

            if (data == null) {
                iterator.previous();
                break;
            }

            if (!(
                    isSetVariable(data) |
                            isIf(data, indent) |
                            isFor(data, indent) |
                            isWhile(data, indent) |
                            isWriteLn(data) |
                            isReadLn(data)
            )) {
                throw new Exception("Ошибка в чтении");
            }
        }
    }

    // Инициализация переменных
    private void initializationVariables() throws Exception {
        if (!deleteStartIndent(getCurrentString(), 1).equals("begin")) {
            iterator.previous();
            while (iterator.hasNext()) {
                skipLineTranslation();

                String text = getCurrentString();

                // Выход из цикла
                if (text.matches("^\t(?!\t)begin$")) {
                    iterator.previous();
                    break;
                }

                String data = deleteStartIndent(text, 1);
                data = deleteSemicolon(data);

                parseVariables(data);
                addToResult(2, ";");
            }
        }
    }

    // Парсим переменные
    private void parseVariables(String data) throws Exception {
        String[] rawVariables = data.split("\s?,\s?|\s|:");

        if (rawVariables.length < 2) {
            throw new Exception("Неправильный синтаксис переменных.");
        }

        String type = rawVariables[rawVariables.length - 1];

        if (isSupportTypeVariable(type)) {
            throw new Exception("Тип: " + type + " не поддерживается.");
        }

        for (int i = 0; i < rawVariables.length - 1; i++) {
            String name = rawVariables[i];

            if (!name.matches("^" + variableRegex + "$")) {
                throw new Exception("Не поддерживаемое название переменной.");
            }

            if (checkExistVariable(name)) {
                throw new Exception("Переменная " + name + " уже существует.");
            }

            variables.add(new Variable(name, type));
            addToResult(3, name);
            if (rawVariables.length - 1 - i > 1) {
                addToResult(2, ",");
            }
        }
        addToResult(2, ":");
        addToResult(1, type);
    }

    // Проверка существует ли переменная или нет
    private Boolean checkExistVariable(String name) {
        for (Variable variable : variables) {
            if (variable.name.equals(name)) {
                return true;
            }
        }

        return false;
    }

    // Проверка поддерживается ли переменная
    private Boolean isSupportTypeVariable(String type) {
        return variableTypes.equals(type);
    }

    private Boolean isSetVariable(String data) throws Exception {
        String regex = "^(" + variableRegex + ")\s?:=\s?(.+);$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.matches()) {
            String name = matcher.group(1);
            String value = matcher.group(2);

            if (!checkExistVariable(name)) {
                throw new Exception("Переменной " + name + " не существует.");
            }

            Variable variable = null;
            for (Variable variableData : variables) {
                if (variableData.name.equals(name)) {
                    variable = variableData;
                }
            }

            assert variable != null;
            addToResult(3, variable.name);
            addToResult(2, ":");
            addToResult(2, "=");

            // Проверяем вставляемое значение
            switch (variable.type) {
                case "int", "float" -> parseOperationWithNumber(value, variable.type);
                case "bool" -> parseOperationWithBoolean(value, variable.type);
            }

            addToResult(2, ";");
            return true;
        } else {
            return false;
        }
    }

    // Проверка выражения с числом
    private Boolean parseOperationWithNumber(String data, String type) throws Exception {
        String[] raw = data.split("\s", 3);

        if (type.equals("int")) {
            checkOperationWithIntType(data, type);
        }

        if (!getTypeNumber(data).equals("")) {
            addToResult(4, data);

            return true;
        } else if (raw.length == 3) {
            checkMathematicalExpression(raw[0], raw[1], raw[2], mathematicalOperators);

            return true;
        } else {
            throw new Exception("Это не операция с числом");
        }
    }

    // Проверка математического выражения
    private void checkMathematicalExpression(String e1, String operator, String e2, List<String> operators) throws Exception {
        if (operators.equals(operator)) {
            throw new Exception(operator + " математического оператора не существует");
        }

        checkNumberOrVariable(e1);

        addToResult(2, operator);

        checkNumberOrVariable(e2);
    }

    // Проверка число, либо переменная
    private void checkNumberOrVariable(String data) throws Exception {
        if (!getTypeNumber(data).equals("")) {
            addToResult(4, data);
        } else if (checkExistVariable(data)) {
            addToResult(3, data);
        } else if (parseOperationWithNumber(data, "int")) {

        } else {
            throw new Exception(data + " не число, либо переменная");
        }
    }

    // Проверка операции с переменной типа int
    private void checkOperationWithIntType(String data, String operation) throws Exception {
        if (data != null && getTypeNumber(data).equals("e")) {
            throw new Exception("Нельзя присвоить экспоненциальную форму в int.");
        }

        if (operation.equals("/")) {
            throw new Exception("Нельзя делить в int.");
        }
    }

    // Получение типа числа
    private String getTypeNumber(String data) {
        if (data.matches("^([0-9]+)([eE][-+]?[0-9]+)$")) { // Экспоненциальная форма
            return "e";
        }

        if (data.matches("^[+-]?[0-9]+$")) { // десятичное число
            return "d";
        }

        var substring = data.substring(data.length() - 1);
        switch (substring) {
            case "b": // двоичное
                if (data.matches("^[01]+b$")) {
                    return "b";
                }
                break;
            case "o": // восьмиричное
                if (data.matches("^[0-7]+o$")) {
                    return "o";
                }
                break;
            case "d": // десятичный
                if (data.matches("^[0-9]+d$")) {
                    return "d";
                }
                break;
            case "h": // шестнадцатирич
                if (data.matches("^[0-9a-fA-F]+h$")) {
                    return "h";
                }
                break;
        }

        return "";
    }

    private void parseOperationWithBoolean(String data, String type) throws Exception {
        if (!(data.equals("true") | data.equals("false"))) {
            throw new Exception("В переменную можно записывать только bool");
        }

        addToResult(1, data);
    }

    // Проверка If
    private Boolean isIf(String data, int indent) throws Exception {
        String regex = "^if\s?\\((.+)\\)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.matches()) {
            addToResult(1, "if");
            addToResult(2, "(");

            String[] head = matcher.group(1).split("\s", 3);

            checkHeadIf(head);

            parseBody(indent + 1);

            addToResult(2, ")");

            if (parseString(getCurrentString(), indent).equals("else")) {
                addToResult(1, "else");

                parseBody(indent + 1);
            }

            return true;
        } else {
            return false;
        }
    }

    // Проверка головы If
    private void checkHeadIf(String[] data) throws Exception {
        List<String> mathematicalOperators = Arrays.asList("!=", "==", "<", "<=", ">", ">=", "||", "&&");

        checkMathematicalExpression(data[0], data[1], data[2], mathematicalOperators);
    }

    // Проверка for
    private Boolean isFor(String data, int indent) throws Exception {
        String regex = "^for (.+) to (.+) step (.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.matches()) {
            addToResult(1, "for");
            addToResult(2, "to");

            checkHeadFor(matcher.group(1), matcher.group(2), matcher.group(3));

            parseBody(indent + 1);

            return true;
        } else {
            return false;
        }
    }

    // Проверка головы for
    private void checkHeadFor(String from, String to, String step) throws Exception {
        isSetVariable(from);
        checkNumberOrVariable(to);
        checkNumberOrVariable(step);
    }

    // Проверка while
    private Boolean isWhile(String data, int indent) throws Exception {
        String regex = "^while\s(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.matches()) {
            addToResult(1, "while");
            addToResult(1, "(");

            String let = matcher.group(1);

            isSetVariable(let);

            addToResult(1, ")");

            parseBody(indent + 1);

            return true;
        } else {
            return false;
        }
    }

    // Проверка writeln
    private Boolean isWriteLn(String data) {
        Boolean result = data.matches("^writeln (\\(?(\"[^\"]+\"|[A-Za-z]+)\\)?,? ?)+;$");
        if (result) {
            addToResult(1, "writeln");
        }
        return result;
    }

    // Проверка readln
    private Boolean isReadLn(String data) {
        Boolean result = data.matches("readln \\(?([a-zA-z0-9]+,?\\s?)+\\)?;$");
        if (result) {
            addToResult(1, "readln");
        }
        return result;
    }

    // Добавление результата анализа в список
    private void addToResult(Integer id, String element) {
        int idElement = -1;

        switch (id) {
            case 1: // Ключевые слова
                idElement = keywords.indexOf(element);
                break;
            case 2: // Разделители
                idElement = separators.indexOf(element);
                break;
            case 3: // Идентификаторы
                for (int i = 0; i < variables.size(); i++) {
                    if (variables.get(i).name.equals(element)) {
                        idElement = i;
                        break;
                    }
                }
                break;
            case 4: // Числа
                int index = -1;
                for (int i = 0; i < numbers.size(); i++) {
                    if (numbers.get(i).original.equals(element)) {
                        index = i;
                    }
                }
                if (index == -1) {
                    numbers.add(new NumberBinary(element));
                    index = numbers.size() - 1;
                }

                idElement = index;
                break;
            default:
                return;
        }


        if (idElement == -1) {
            return;
        }

        idElement++;
        result.add(new AnalyzerResult(id, idElement));

    }

    // Получение текущей строки с удалением лишних пробелов в конце и переход на следующую
    private String getCurrentString() {
        return deleteEndIndent(iterator.next());
    }

    // Пропуск пустой строки
    private Boolean skipLineTranslation() {
        if (iterator.hasNext() && iterator.next().trim().equals("")) {
            return skipLineTranslation();
        } else if (iterator.hasPrevious()) {
            iterator.previous();
            return false;
        } else {
            return false;
        }
    }

    // Получение текущей строки с удалением лишних пробелов в начале
    private String deleteStartIndent(String data, Integer indent) throws Exception {
        String pattern = "^\t{" + indent + "}(?!\t)";

        if (!data.matches(pattern + ".*$")) {
            throw new Exception("Неправильный отступ");
        }

        return data.replaceFirst(pattern, "");
    }

    // Получение текущей строки с удалением лишних пробелов в конце
    private String deleteEndIndent(String data) {
        return data.replaceFirst("[\t\s]+$", "");
    }

    // Удаление ";" в конце строки
    private String deleteSemicolon(String data) {
        return data.substring(0, data.length() - 1);
    }

    // Проверка строки в теле программы
    private String parseString(String data, int indent) throws Exception {
        if (data.matches("^\t{" + indent + "}(?!\t).+$")) { // проверяем отступ слева
            data = data.substring(indent);
        } else if (data.matches("^\t{" + (indent - 1) + "}(?!\t).+$")) { // выход из тела
            return null;
        } else {
            throw new Exception("Неизвестный код, тело должно быть на " + indent + " уровне");
        }

        return data;
    }
}

