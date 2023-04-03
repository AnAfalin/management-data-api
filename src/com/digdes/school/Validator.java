package com.digdes.school;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern PATTERN_LONG_DOUBLE = Pattern.compile("(=|!=|>|<|>=|<=)++");
    private static final Pattern PATTERN_BOOLEAN = Pattern.compile("[^><](=|!=)++");
    private static final Pattern PATTERN_STRING = Pattern.compile("(=|!=|ilike|like)++");
    private static final Pattern PATTERN_APOSTROPHE = Pattern.compile("'");

    public static void validationTypeRequest(String request) throws Exception {
        String[] s = request.split(" ");
        if ((s[0].equalsIgnoreCase("insert") ||
                s[0].equalsIgnoreCase("update"))) {

            if (s.length == 1 || !s[1].equalsIgnoreCase("values")) {
                throw new Exception("Запрос должен содержать VALUES");
            }

        } else if (!(s[0].equalsIgnoreCase("insert") ||
                s[0].equalsIgnoreCase("select") ||
                s[0].equalsIgnoreCase("update") ||
                s[0].equalsIgnoreCase("delete"))) {
            throw new Exception("Запрос должен начинаться с 'INSERT', 'SELECT', 'UPDATE', 'DELETE'");
        }
    }

    public static void validationFieldWithValue(String strFieldValue) throws Exception {
        Matcher matchers = PATTERN_APOSTROPHE.matcher(strFieldValue);
        int count = 0;
        while (matchers.find()) {
            count++;
        }

        int indexFirstApostrophe = strFieldValue.trim().indexOf("'");
        int indexSecondApostrophe = strFieldValue.trim().indexOf("'", 1);

        if (indexFirstApostrophe != 0) {
            throw new Exception(("Ошибка в правильности применения одинарных кавычек в запросе \033[97m%s\033[0m. " +
                    "Одинарными кавычками выделяются поля и строковые значения переменных")
                    .formatted(strFieldValue));
        }

        String fieldInStr = strFieldValue
                .substring(indexFirstApostrophe + 1, indexSecondApostrophe)
                .toLowerCase()
                .trim();

        if (!Token.FIELDS.contains(fieldInStr)) {
            throw new Exception("Поля \033[97m%s\033[0m не существует в таблице".formatted(fieldInStr));
        }

        Matcher matcher;

        if (fieldInStr.equalsIgnoreCase("cost") || fieldInStr.equalsIgnoreCase("age")
                || fieldInStr.equalsIgnoreCase("id")) {
            matcher = PATTERN_LONG_DOUBLE.matcher(strFieldValue);

            if(count == 4) {
                throw new Exception(("Ошибка в правильности применения одинарных кавычек в запросе \033[97m%s\033[0m." +
                        "Числовые значения одинарными кавычками не выделяются")
                        .formatted(strFieldValue));
            }

            if (!matcher.find()) {
                throw new Exception("Некорректный тип оператора в запросе\033[97m%s\033[0m".formatted(strFieldValue));
            }

            String value = strFieldValue.split(PATTERN_LONG_DOUBLE.pattern())[1].trim();
            String group = matcher.group();
            if (value.equals("null")) {
                if (!group.equals("=") && !group.equals("!=")) {
                    throw new Exception("Некорректное условие \033[97m%s\033[0m".formatted(strFieldValue));
                }
            } else {
                try {
                    if (fieldInStr.equalsIgnoreCase("cost")) {
                        Double.parseDouble(value);
                    } else {
                        Long.parseLong(value);
                    }
                } catch (NumberFormatException ex) {
                    throw new Exception("Ошибка в запросе \033[97m%s\033[0m".formatted(strFieldValue));
                }
            }

        } else if (fieldInStr.equalsIgnoreCase("active")) {
            matcher = PATTERN_BOOLEAN.matcher(strFieldValue);

            if(count == 4) {
                throw new Exception(("Ошибка в правильности применения одинарных кавычек в запросе \033[97m%s\033[0m." +
                        "Значения логического типа одинарными кавычками не выделяются")
                        .formatted(strFieldValue));
            }

            if (!matcher.find()) {
                throw new Exception("Некорректный тип оператора в запросе \033[97m%s\033[0m".formatted(strFieldValue));
            }
            String value = strFieldValue.split(PATTERN_LONG_DOUBLE.pattern())[1].trim();
            if (!value.equalsIgnoreCase("false") &&
                    !value.equalsIgnoreCase("true") &&
                    !value.equalsIgnoreCase("null")) {
                throw new Exception("Ошибка в запросе \033[97m%s\033[0m".formatted(strFieldValue));
            }

        } else {
            matcher = PATTERN_STRING.matcher(strFieldValue);

            if (count != 4 && !strFieldValue.contains("null")) {
                throw new Exception(("Ошибка в запросе \033[97m%s\033[0m. " +
                        "Поле и значение строковой переменной должны выделяться одинарными кавычками.")
                        .formatted(strFieldValue));
            }

            if (!matcher.find() || strFieldValue.contains(">") || strFieldValue.contains("<")) {
                throw new Exception("Некорректный тип оператора в запросе \033[97m%s\033[0m".formatted(strFieldValue));
            }
            String group = matcher.group();
            String value = strFieldValue.split(PATTERN_STRING.pattern())[1].trim();
            if (value.equalsIgnoreCase("null") && (!group.equals("=") && !group.equals("!="))) {
                throw new Exception("Некорректное условие \033[97m%s\033[0m".formatted(strFieldValue));
            }
        }
    }
}