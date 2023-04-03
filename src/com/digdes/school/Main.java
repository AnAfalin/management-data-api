package com.digdes.school;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String... args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();
        try {
            List<Map<String, Object>> execute1 = starter.execute(
                    "INSERT VALUES 'lastName' = 'Федоров Степан', 'id'=1, 'age'=40, 'cost'=5.0, 'active'=true");
            List<Map<String, Object>> execute2 = starter.execute(
                    "insert values 'lastName' = 'Иванов', 'id'=2, 'cost'=null, 'active'=null");
            List<Map<String, Object>> execute3 = starter.execute(
                    "inSert Values 'lastName' = 'Петрова', 'id'=3, 'age'=20, 'cost'=null, 'active'=false");
            List<Map<String, Object>> execute4 = starter.execute(
                    "insert values 'id'=4, 'age'=25, 'cost'=3.5, 'active'=false");

            starter.printTable();

            List<Map<String, Object>> execute5 = starter.execute(
                    "Update values 'lastName'='Инкогнито' where 'lastName'=null");
            starter.printTable(execute5);

            List<Map<String, Object>> execute6 = starter.execute(
                    "Update values 'cost'=0.0 where 'cost'=null");
            starter.printTable(execute6);

            List<Map<String, Object>> execute7 = starter.execute(
                    "update VALUES 'active'=true where 'cost'>=3");
            starter.printTable(execute7);

            List<Map<String, Object>> execute8 = starter.execute(
                    "select where 'age'> 18");
            starter.printTable(execute8);

            List<Map<String, Object>> execute9 = starter.execute(
                    "DELETE where 'age'=null or 'lastname'='Инкогнито'");
            starter.printTable(execute9);

            List<Map<String, Object>> execute10 = starter.execute(
                    "DELETE where 'lastname' like '%ов'");
            starter.printTable(execute10);

            starter.printTable();


            // проверка ошибок

            // Запрос должен содержать VALUES
            // starter.execute("Insert 'lastname' = 'Тестин', 'id'=5, 'age'=20, 'cost'=5.0, 'active'=false");

            // Поля name не существует в таблице
            // starter.execute("inSert Values 'name' = 'Тестин', 'id'=5, 'age'=20, 'cost'=5.0, 'active'=false");

            // Ошибка в правильности применения одинарных кавычек в запросе lastname = 'Тестин'.
            // Одинарными кавычками выделяются поля и строковые значения переменных
            // starter.execute("Insert values lastname = 'Тестин', 'id'=5, 'age'=20, 'cost'=5.0, 'active'=false");

            // Ошибка в запросе 'id'=пять
            // starter.execute("Insert values 'lastname' = 'Тестин', 'id'=пять, 'age'=20, 'cost'=5.0, 'active'=false");

            // В части запроса 'age'>20 на установление значений должен быть оператор '='
            // starter.execute("Insert values 'lastname' = 'Тестин', 'id'=5, 'age'>20, 'cost'=5.0, 'active'=false");

            // Ошибка в запросе 'active'=1
            // starter.execute("Insert values 'lastname' = 'Тестин', 'id'=5, 'age'=20, 'cost'=5.0, 'active'=1");

            // Некорректный тип оператора в запросе 'active'<= null
            // starter.execute("Update values 'lastname' = null where 'active'<= null");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}