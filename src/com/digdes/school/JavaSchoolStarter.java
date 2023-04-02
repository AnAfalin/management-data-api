package com.digdes.school;

import java.util.*;

public class JavaSchoolStarter {
    private final List<Map<String, Object>> data = new LinkedList<>();
    private final Handler handler = new Handler(data);

    public JavaSchoolStarter() {
    }

    public List<Map<String, Object>> execute(String request) throws Exception {
        String[] requestParts = request.split(" ");

        Validator.validationTypeRequest(request);

        return switch (requestParts[0].toLowerCase()) {
            case "insert" -> handler.insert(request);
            case "update" -> handler.update(request);
            case "delete" -> handler.delete(request);
            case "select" -> handler.select(request);
            default -> throw new Exception(
                    "Запрос должен начинаться с ключевых слов: \"INSERT\", \"UPDATE\", \"DELETE\", \"SELECT\"");
        };
    }

    public void printTable() {
        LinkedList<String> titles = Token.FIELDS;

        String leftAlignFormat = "| %-4s | %-23s | %-4s | %-6s | %-6s |%n";

        System.out.format("+------+-------------------------+------+--------+--------+%n");
        System.out.format("| id   | lastName                | age  | cost   | active |%n");
        System.out.format("+------+-------------------------+------+--------+--------+%n");

        for (int i = 1; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            System.out.printf(leftAlignFormat,
                    row.get(titles.get(0)), row.get(titles.get(1)), row.get(titles.get(2)),
                    row.get(titles.get(3)), row.get(titles.get(4)));
        }

        System.out.format("+------+-------------------------+------+--------+--------+%n");
        System.out.println();
    }

    public void printTable(List<Map<String, Object>> data) {
        LinkedList<String> titles = Token.FIELDS;

        String leftAlignFormat = "| %-4s | %-23s | %-4s | %-6s | %-6s |%n";

        System.out.format("+------+-------------------------+------+--------+--------+%n");
        System.out.format("| id   | lastName                | age  | cost   | active |%n");
        System.out.format("+------+-------------------------+------+--------+--------+%n");

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            System.out.printf(leftAlignFormat,
                    row.get(titles.get(0)), row.get(titles.get(1)), row.get(titles.get(2)),
                    row.get(titles.get(3)), row.get(titles.get(4)));
        }

        System.out.format("+------+-------------------------+------+--------+--------+%n");
        System.out.println();
    }
}