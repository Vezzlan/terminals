package org.example.model;

import java.util.Map;

public record CityReport(Map<String, Integer> customerAges) {

    public int customerCount() {
        return customerAges.size();
    }

    public double averageAge() {
        return customerAges.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }
}

