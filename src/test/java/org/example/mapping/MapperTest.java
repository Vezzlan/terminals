package org.example.mapping;

import org.example.model.CityAggregate;
import org.example.model.CityReport;
import org.example.model.Customer;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MapperTest {

    private final Mapper mapper = new Mapper();

    private final List<Customer> customers = List.of(
            new Customer("Alice", 31, true, List.of("alice@example.com"), "Berlin", Set.of("Books", "Electronics")),
            new Customer("Bob", 17, false, List.of("bob@example.com"), "Berlin", Set.of("Toys")),
            new Customer("Carol", 26, true, List.of("carol@example.com"), "Paris", Set.of("Books", "Beauty")),
            new Customer("Dan", 40, false, List.of("dan@example.com"), "Paris", Set.of("Garden")),
            new Customer("Anthony", 7, false, List.of("anthony@example.com"), "Umeå", Set.of("Electronics", "Cars", "Carpets"))
    );

    @Test
    public void testMapToNameAndCustomer() {
        Map<String, Customer> result = mapper.mapToNameAndCustomer(customers);
        assertEquals(5, result.size());
        assertEquals("Alice", result.get("Alice").name());
        assertEquals("Dan", result.get("Dan").name());
    }

    @Test
    public void testMapToNameAndCity() {
        Map<String, String> result = mapper.mapToNameAndCity(customers);
        assertEquals("Berlin", result.get("Alice"));
        assertEquals("Paris", result.get("Dan"));
    }

    @Test
    public void testMapToNameAndAge() {
        Map<String, Integer> result = mapper.mapToNameAndAge(customers);
        assertEquals((Integer) 31, result.get("Alice"));
        assertEquals((Integer) 17, result.get("Bob"));
    }

    @Test
    public void testPartitionByVIP() {
        Map<Boolean, List<Customer>> result = mapper.partitionByVIP(customers);
        assertEquals(2, result.get(true).size());
        assertEquals(3, result.get(false).size());
    }

    @Test
    public void testPartitionByVIPAndCount() {
        Map<Boolean, Long> result = mapper.partitionByVIPAndCount(customers);
        assertEquals(2L, (long) result.get(true));
        assertEquals(3L, (long) result.get(false));
    }

    @Test
    public void testPartitionByAdult() {
        Map<Boolean, List<Customer>> result = mapper.partitionByAdult(customers);
        assertEquals(3, result.get(true).size());
        assertEquals(2, result.get(false).size());
        assertEquals("Bob", result.get(false).getFirst().name());
    }

    @Test
    public void testGroupByCityWithNameToAge() {
        // groupingBy(city) with toMap(name, age) as downstream
        // Result: { "Berlin" -> {"Alice"->31, "Bob"->17}, "Paris" -> {"Carol"->26, "Dan"->40} }
        Map<String, Map<String, Integer>> result = mapper.groupByCityWithNameToAge(customers);

        assertEquals(2, result.get("Berlin").size());
        assertEquals((Integer) 31, result.get("Berlin").get("Alice"));
        assertEquals((Integer) 17, result.get("Berlin").get("Bob"));

        assertEquals(2, result.get("Paris").size());
        assertEquals((Integer) 26, result.get("Paris").get("Carol"));
        assertEquals((Integer) 40, result.get("Paris").get("Dan"));
    }

    @Test
    public void testGroupByNameAgeToCustomers() {
        Map<String, List<Customer>> result = mapper.groupByNameAgeToCustomers(customers);

        assertEquals(1, result.get("Alice_31").size());
        assertEquals(1, result.get("Bob_17").size());
        assertEquals(1, result.get("Carol_26").size());
        assertEquals(1, result.get("Dan_40").size());
        assertEquals(1, result.get("Anthony_7").size());
    }

    @Test
    public void testGroupByCityAsCityReport() {
        Map<String, CityReport> result = mapper.groupByCityAsCityReport(customers);

        CityReport berlin = result.get("Berlin");
        assertEquals(2, berlin.customerCount());
        assertEquals((Integer) 31, berlin.customerAges().get("Alice"));
        assertEquals((Integer) 17, berlin.customerAges().get("Bob"));
        assertEquals(24.0, berlin.averageAge(), 0.01);

        CityReport paris = result.get("Paris");
        assertEquals(2, paris.customerCount());
        assertEquals((Integer) 26, paris.customerAges().get("Carol"));
        assertEquals((Integer) 40, paris.customerAges().get("Dan"));
        assertEquals(33.0, paris.averageAge(), 0.01);
    }

    @Test
    public void testGroupByCityMappingToNames() {
        Map<String, List<String>> result = mapper.groupByCityMappingToNames(customers);
        assertEquals(List.of("Alice", "Bob"), result.get("Berlin"));
        assertEquals(List.of("Carol", "Dan"), result.get("Paris"));
    }

    @Test
    public void testGroupByCityJoiningNames() {
        Map<String, String> result = mapper.groupByCityJoiningNames(customers);
        assertEquals("Alice, Bob", result.get("Berlin"));
        assertEquals("Carol, Dan", result.get("Paris"));
        assertEquals("Anthony", result.get("Umeå"));
    }

    @Test
    public void testGroupByCitySortedUnmodifiableNames() {
        List<Customer> unordered = List.of(
                new Customer("Bob", 17, false, List.of("bob@example.com"), "Berlin", Set.of("Toys")),
                new Customer("Alice", 31, true, List.of("alice@example.com"), "Berlin", Set.of("Books")),
                new Customer("Dan", 40, false, List.of("dan@example.com"), "Paris", Set.of("Garden")),
                new Customer("Carol", 26, true, List.of("carol@example.com"), "Paris", Set.of("Beauty"))
        );

        Map<String, List<String>> result = mapper.groupByCitySortedUnmodifiableNames(unordered);

        // Finisher sorts each grouped list alphabetically
        assertEquals(List.of("Alice", "Bob"), result.get("Berlin"));
        assertEquals(List.of("Carol", "Dan"), result.get("Paris"));
    }

    @Test
    public void testPartitionByVIPMappingToNames() {
        Map<Boolean, Set<String>> result = mapper.partitionByVIPMappingToNames(customers);
        assertEquals(Set.of("Alice", "Carol"), result.get(true));
        assertEquals(Set.of("Bob", "Dan", "Anthony"), result.get(false));
    }

    @Test
    public void testGroupByCityMappingToCategories() {
        Map<String, Set<String>> result = mapper.groupByCityMappingToCategories(customers);
        assertEquals(Set.of("Books", "Electronics", "Toys"), result.get("Berlin"));
        assertEquals(Set.of("Books", "Beauty", "Garden"), result.get("Paris"));
    }

    @Test
    public void testGroupByCityFilteringVIPs() {
        Map<String, List<Customer>> result = mapper.groupByCityFilteringVIPs(customers);
        assertEquals(1, result.get("Berlin").size());
        assertEquals("Alice", result.get("Berlin").getFirst().name());
        assertEquals(1, result.get("Paris").size());
        assertEquals("Carol", result.get("Paris").getFirst().name());
    }

    @Test
    public void testPartitionByAdultFilteringVIPs() {
        Map<Boolean, List<Customer>> result = mapper.partitionByAdultFilteringVIPs(customers);
        assertEquals(List.of(customers.get(0), customers.get(2)), result.get(true));
        assertEquals(List.of(), result.get(false));
    }

    @Test
    public void testGroupByCityCountingAdults() {
        Map<String, Long> result = mapper.groupByCityCountingAdults(customers);
        assertEquals(1L, (long) result.get("Berlin"));
        assertEquals(2L, (long) result.get("Paris"));
    }

    @Test
    public void testCityAggregates() {
        List<Map<String, Object>> result = mapper.cityAggregates(customers);
        List<Map<String, List<String>>> result1 = mapper.cityAggregates1(customers);
        List<CityAggregate> result2 = mapper.cityAggregates2(customers);
        List<CityAggregate> result3 = mapper.cityAggregates3(customers);

        System.out.println("Result 1: " + result);
        System.out.println("Result 2: " + result1);
        System.out.println("Result 3: " + result2);
        System.out.println("Result 4: " + result3);
        assertEquals(3, result.size());

    }

}