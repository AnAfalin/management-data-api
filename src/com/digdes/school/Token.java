package com.digdes.school;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Token {
    public final static LinkedList<String> FIELDS = new LinkedList<>(List.of("id", "lastname", "age", "cost", "active"));

    public static Map<String, Object> firstRowMap() {
        Map<String, Object> row = new HashMap<>();
        for (String field : FIELDS) {
            row.put(field, field);
        }
        return row;
    }

    public static Map<String, Object> newRowToMap(String request) throws Exception {
        Map<String, Object> row = rowToMap(request);

        for (String field : FIELDS) {
            row.putIfAbsent(field, null);
        }

        return row;
    }

    public static Map<String, Object> updatableRowToMap(String request) throws Exception {
        return rowToMap(request);
    }

    private static Map<String, Object> rowToMap(String request) throws Exception {
        Map<String, Object> row = new HashMap<>();

        String[] requestPart = request.split(",");
        for (String fieldWithValue : requestPart) {

            fieldWithValue = fieldWithValue.trim();
            Validator.validationFieldWithValue(fieldWithValue);

            String[] splitStr = fieldWithValue.trim().split("=");

            if (splitStr.length != 2) {
                throw new Exception(("В части запроса \033[97m%s\033[0m " +
                        "на присвоение значения должен быть оператор '='").formatted(fieldWithValue));
            }

            String field = splitStr[0].toLowerCase().trim().replace("'", "");
            String value = splitStr[1].trim().replace("'", "");

            if (field.equalsIgnoreCase(FIELDS.get(0))) {
                row.put(FIELDS.get(0), value.equalsIgnoreCase("null") ? null : Long.parseLong(value.trim()));
            } else if (field.equalsIgnoreCase(FIELDS.get(2))) {
                row.put(FIELDS.get(2), value.equalsIgnoreCase("null") ? null : Long.parseLong(value.trim()));
            } else if (field.equalsIgnoreCase(FIELDS.get(3))) {
                row.put(FIELDS.get(3), value.equalsIgnoreCase("null") ? null : Double.parseDouble(value.trim()));
            } else if (field.equalsIgnoreCase(FIELDS.get(4))) {
                row.put(FIELDS.get(4), value.equalsIgnoreCase("null") ? null : Boolean.parseBoolean(value.trim()));
            } else if (field.equalsIgnoreCase(FIELDS.get(1))) {
                row.put(FIELDS.get(1), value);
            }
        }
        return row;
    }
}