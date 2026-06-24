package org.example.model;

import java.util.List;

public record CityAggregate(
        String city,
        List<String> names
) {}
