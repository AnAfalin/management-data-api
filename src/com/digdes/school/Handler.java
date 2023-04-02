package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Handler {
    private final List<Map<String, Object>> data;

    public Handler(List<Map<String, Object>> data) {
        this.data = data;
        addFirstRow();
    }

    public void addFirstRow() {
        Map<String, Object> firstRow = Token.firstRowMap();
        data.add(firstRow);
    }

    public List<Map<String, Object>> insert(String request) throws Exception {
        request = request.substring("insert values".length() + 1);

        Map<String, Object> newRow = Token.newRowToMap(request);
        data.add(newRow);

        return data;
    }

    public List<Map<String, Object>> update(String request) throws Exception {
        int startValue = request.toLowerCase().indexOf("values") + "values".length();
        int endValue = request.toLowerCase().indexOf("where");

        endValue = endValue == -1 ? request.length() : endValue;

        String parameters = request.trim().substring(startValue, endValue);

        Map<String, Object> newParameters = Token.updatableRowToMap(parameters);

        if (endValue == request.length()) {
            data
                    .stream()
                    .skip(1)
                    .forEach(el -> el.putAll(newParameters));
            return data;
        }

        String conditions = request.substring(endValue + "where".length()).trim();

        if (conditions.toLowerCase().contains("and")) {
            Set<Map<String, Object>> resultFilter = filterAnd(conditions);
            data
                    .stream()
                    .skip(1)
                    .filter(resultFilter::contains)
                    .forEach(map -> map.putAll(newParameters));
            return resultFilter.stream().toList();
        }

        if (conditions.toLowerCase().contains("or")) {
            Set<Map<String, Object>> resultFilter = filterOr(conditions);
            data
                    .stream()
                    .skip(1)
                    .filter(resultFilter::contains)
                    .forEach(map -> map.putAll(newParameters));
            return resultFilter.stream().toList();
        }

        List<Map<String, Object>> res = filter(conditions);
        data
                .stream()
                .filter(res::contains)
                .forEach(map -> map.putAll(newParameters));
        return res;
    }

    public List<Map<String, Object>> select(String request) throws Exception {
        int startWhere = request.toLowerCase().indexOf("where");
        startWhere = startWhere == -1 ? request.length() : startWhere;

        if (startWhere == request.length()) {
            return data;
        }

        String conditions = request.substring(startWhere + "where".length()).trim();

        if (conditions.toLowerCase().contains("and")) {
            Set<Map<String, Object>> resultFilter = filterAnd(conditions);
            return resultFilter.stream().toList();
        }

        if (conditions.toLowerCase().contains("or")) {
            Set<Map<String, Object>> resultFilter = filterOr(conditions);
            return resultFilter.stream().toList();
        }

        List<Map<String, Object>> res = filter(conditions);
        return res;
    }

    public List<Map<String, Object>> delete(String request) throws Exception {
        int startWhere = request.toLowerCase().indexOf("where");
        startWhere = startWhere == -1 ? request.length() : startWhere;

        if (startWhere == request.length()) {
            data.clear();
            addFirstRow();
            return data;
        }

        String conditions = request.substring(startWhere + "where".length()).trim();

        if (conditions.toLowerCase().contains("and")) {
            Set<Map<String, Object>> resultFilter = filterAnd(conditions);
            for (Map<String, Object> map : resultFilter) {
                data.remove(map);
            }
            return resultFilter.stream().toList();
        }

        if (conditions.toLowerCase().contains("or")) {
            Set<Map<String, Object>> resultFilter = filterOr(conditions);
            for (Map<String, Object> map : resultFilter) {
                data.remove(map);
            }
            return resultFilter.stream().toList();
        }

        List<Map<String, Object>> res = filter(conditions);
        for (Map<String, Object> map : res) {
            data.remove(map);
        }
        return res;
    }

    private Set<Map<String, Object>> filterAnd(String conditions) throws Exception {
        int index = conditions.toLowerCase().indexOf("and");

        String firstCond = conditions.substring(0, index).trim();
        String secondCond = conditions.substring(index + "and".length() + 1).trim();

        Set<Map<String, Object>> set1 = new HashSet<>(filter(firstCond));
        Set<Map<String, Object>> set2 = new HashSet<>(filter(secondCond));

        set1.retainAll(set2);

        return set1;
    }

    private Set<Map<String, Object>> filterOr(String conditions) throws Exception {
        int index = conditions.toLowerCase().indexOf("or");

        String firstCond = conditions.substring(0, index).trim();
        String secondCond = conditions.substring(index + "or".length() + 1).trim();

        Set<Map<String, Object>> set1 = new HashSet<>(filter(firstCond));
        Set<Map<String, Object>> set2 = new HashSet<>(filter(secondCond));

        set1.addAll(set2);

        return set1;
    }

    private List<Map<String, Object>> filter(String condition) throws Exception {
        String filterCondition = condition.toLowerCase();

        if (filterCondition.contains("age") || filterCondition.contains("id")) {
            return filterLong(condition);
        } else if (filterCondition.contains("active")) {
            return filterBoolean(condition);
        } else if (filterCondition.contains("cost")) {
            return filterDouble(condition);
        } else {
            return filterString(condition);
        }
    }

    private List<Map<String, Object>> filterLong(String condition) throws Exception {
        Validator.validationFieldWithValue(condition);

        Pattern patternLong = Pattern.compile("(=|!=|>|<|>=|<=)++");
        Matcher matcher = patternLong.matcher(condition);

        String operator = "";
        if (matcher.find()) {
            operator = matcher.group();
        }

        String[] conditionParts = condition.split(patternLong.pattern());

        String field = conditionParts[0].replace("'", "").trim().toLowerCase();
        String value = conditionParts[1].trim().replace("'", "");
        Long longValue = value.equalsIgnoreCase("null") ? null : Long.parseLong(value);

        if (operator.equals("<=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Long) map.get(field)).compareTo(longValue);
                                return compare <= 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals(">=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Long) map.get(field)).compareTo(longValue);
                                return compare >= 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("<")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Long) map.get(field)).compareTo(longValue);
                                return compare < 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals(">")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Long) map.get(field)).compareTo(longValue);
                                return compare > 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("!=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && longValue == null) {
                                    return false;
                                } else if ((map.get(field) == null && longValue != null)
                                        || (map.get(field) != null && longValue == null)) {
                                    return true;
                                }
                                return !map.get(field).equals(longValue);
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && longValue == null) {
                                    return true;
                                } else if ((map.get(field) == null && longValue != null)
                                        || (map.get(field) != null && longValue == null)) {
                                    return false;
                                }
                                return map.get(field).equals(longValue);

                            }
                    )
                    .collect(Collectors.toList());
        }

        return data;
    }

    private List<Map<String, Object>> filterDouble(String condition) throws Exception {
        Validator.validationFieldWithValue(condition);

        Pattern patternDouble = Pattern.compile("(=|!=|>|<|>=|<=)++");
        Matcher matcher = patternDouble.matcher(condition);

        String operator = "";
        if (matcher.find()) {
            operator = matcher.group();
        }

        String[] conditionParts = condition.split(patternDouble.pattern());

        String field = conditionParts[0].replace("'", "").trim().toLowerCase();
        String value = conditionParts[1].trim().replace("'", "");
        Double doubleValue = value.equalsIgnoreCase("null") ? null : Double.parseDouble(value);

        if (operator.equals("<=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Double) map.get(field)).compareTo(doubleValue);
                                return compare <= 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals(">=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Double) map.get(field)).compareTo(doubleValue);
                                return compare >= 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("<")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Double) map.get(field)).compareTo(doubleValue);
                                return compare < 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals(">")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null) {
                                    return false;
                                }
                                int compare = ((Double) map.get(field)).compareTo(doubleValue);
                                return compare > 0;
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("!=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && doubleValue == null) {
                                    return false;
                                } else if ((map.get(field) == null && doubleValue != null)
                                        || (map.get(field) != null && doubleValue == null)) {
                                    return true;
                                }
                                return !map.get(field).equals(doubleValue);
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && doubleValue == null) {
                                    return true;
                                } else if ((map.get(field) == null && doubleValue != null)
                                        || (map.get(field) != null && doubleValue == null)) {
                                    return false;
                                }
                                return map.get(field).equals(doubleValue);
                            }
                    )
                    .collect(Collectors.toList());
        }

        return data;
    }

    private List<Map<String, Object>> filterBoolean(String condition) throws Exception {
        Validator.validationFieldWithValue(condition);

        Pattern patternBoolean = Pattern.compile("(=|!=)++");
        Matcher matcher = patternBoolean.matcher(condition);

        String operator = "";
        if (matcher.find()) {
            operator = matcher.group();
        }

        String[] conditionParts = condition.split(patternBoolean.pattern());

        String field = conditionParts[0].replace("'", "").trim().toLowerCase();
        String value = conditionParts[1].trim().replace("'", "").trim();
        Boolean booleanValue = value.equalsIgnoreCase("null") ? null : Boolean.parseBoolean(value);


        if (operator.equals("!=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && booleanValue == null) {
                                    return false;
                                } else if ((map.get(field) == null && booleanValue != null)
                                        || (map.get(field) != null && booleanValue == null)) {
                                    return true;
                                }
                                return !map.get(field).equals(booleanValue);
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && booleanValue == null) {
                                    return true;
                                } else if ((map.get(field) == null && booleanValue != null)
                                        || (map.get(field) != null && booleanValue == null)) {
                                    return false;
                                }
                                return map.get(field).equals(booleanValue);
                            }
                    )
                    .collect(Collectors.toList());
        }
        return data;
    }

    private List<Map<String, Object>> filterString(String condition) throws Exception {
        Validator.validationFieldWithValue(condition);

        Pattern patternString = Pattern.compile("(=|!=|ilike|like)++");
        Matcher matcher = patternString.matcher(condition);

        String operator = "";
        if (matcher.find()) {
            operator = matcher.group();
        }

        String[] conditionParts = condition.split(patternString.pattern());

        String field = conditionParts[0].replace("'", "").trim().toLowerCase();
        String value = conditionParts[1].trim().replace("'", "");
        String stringValue = value.equalsIgnoreCase("null") ? null : value;

        if (operator.equals("=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && stringValue == null) {
                                    return true;
                                } else if ((map.get(field) == null && stringValue != null)
                                        || (map.get(field) != null && stringValue == null)) {
                                    return false;
                                }
                                return map.get(field).equals(stringValue);
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("!=")) {
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                                if (map.get(field) == null && stringValue == null) {
                                    return false;
                                } else if ((map.get(field) == null && stringValue != null)
                                        || (map.get(field) != null && stringValue == null)) {
                                    return true;
                                }
                                return !map.get(field).equals(stringValue);
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (operator.equals("like")) {
            if (!value.contains("%")) {
                return data
                        .stream()
                        .skip(1)
                        .filter(map -> {
                            if (map.get(field) == null && stringValue == null) {
                                return true;
                            } else if ((map.get(field) == null && stringValue != null)
                                    || (map.get(field) != null && stringValue == null)) {
                                return false;
                            }
                            return map.get(field).equals(stringValue);
                        })
                        .collect(Collectors.toList());
            }
            String regexLike = getRegex(value);
            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                        if ((map.get(field) == null && stringValue == null)
                                || (map.get(field) == null && stringValue != null)
                                || (map.get(field) != null && stringValue == null)) {
                            return false;
                        }
                        return ((String) map.get(field)).matches(regexLike);
                    })
                    .collect(Collectors.toList());
        }

        if (operator.equals("ilike")) {
            if (!value.contains("%")) {
                return data
                        .stream()
                        .skip(1)
                        .filter(map -> {
                            if (map.get(field) == null && stringValue == null) {
                                return true;
                            } else if ((map.get(field) == null && stringValue != null)
                                    || (map.get(field) != null && stringValue == null)) {
                                return false;
                            }
                            return ((String) map.get(field)).equalsIgnoreCase(stringValue);
                        })
                        .collect(Collectors.toList());
            }
            String regexILike = getRegex(value);

            return data
                    .stream()
                    .skip(1)
                    .filter(map -> {
                        if (map.get(field) == null) {
                            return false;
                        }
                        return ((String) map.get(field)).toLowerCase().matches(regexILike.toLowerCase())
                                || ((String) map.get(field)).toUpperCase().matches(regexILike.toUpperCase());
                    })
                    .collect(Collectors.toList());
        }
        return data;
    }

    private String getRegex(String value) {
        value = value.replace("'", "");

        int indexFirst = value.indexOf("%");
        int indexSecond = value.indexOf("%", indexFirst + 1);

        if (indexFirst == -1) {
            return value.substring(0, indexSecond).concat(".*");
        }

        if (indexSecond == -1) {
            return ".*".concat(value.substring(1));
        }

        return ".*".concat(value.substring(1, indexSecond)).concat(".*");
    }
}