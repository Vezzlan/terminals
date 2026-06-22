package org.example.model;

import java.util.List;
import java.util.Set;

public record Customer(String name, Integer age, Boolean isVIP, List<String> emails, String city, Set<String> purchasedCategories) {
}
