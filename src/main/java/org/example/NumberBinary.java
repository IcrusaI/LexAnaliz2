package org.example;

public class NumberBinary {
    public String original;

    public NumberBinary(String originalParam) {
        original = originalParam;
    }

    public String getBinary() {
        String buf = original.substring(0, original.length() - 1);
        int temp = 0;

        if (original.matches("^[0-9]+$")) { // десятичное число
            temp = Integer.parseInt(original);
        } else if (original.matches("^([0-9]+)([eE][-+]?[0-9]+)$")) { // Экспоненциальная форма
            temp = Double.valueOf(original).intValue();
        } else {
            switch (original.substring(original.length() - 1)) {
                case "b": // Двоичное
                case "d": // Десятичное
                    temp = Integer.parseInt(buf);
                    break;
                case "o": // Восьмеричное
                    temp = Integer.parseInt(buf, 8);
                    break;
                case "h": // Шеснадцатиричное
                    temp = Integer.parseInt(buf, 16);
                    break;
            }
        }

        return Integer.toString(temp, 2);
    }
}
