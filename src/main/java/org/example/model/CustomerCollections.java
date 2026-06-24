package org.example.model;

import java.util.List;
import java.util.Set;

public record CustomerCollections(List<String> emails, Set<String> purchasedCategories) {
}
