package org.example.mapping;


import org.example.model.CityAggregate;
import org.example.model.CityReport;
import org.example.model.Customer;
import org.example.model.CustomerCollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class Mapper {

    public Map<String, Customer> mapToNameAndCustomer(List<Customer> customers) {
        return customers.stream()
                .collect(toMap(Customer::name, Function.identity()));
    }

    public Map<String, String> mapToNameAndCity(List<Customer> customers) {
        return customers.stream()
                .collect(toMap(Customer::name, Customer::city));
    }

    public Map<String, Integer> mapToNameAndAge(List<Customer> customers) {
        return customers.stream()
                .collect(toMap(Customer::name, Customer::age));
    }

    public Map<Boolean, List<Customer>> partitionByVIP(List<Customer> customers) {
        return customers.stream()
                .collect(partitioningBy(Customer::isVIP, toList()));
    }

    public Map<Boolean, List<Customer>> partitionByVIP_SameAsAbove(List<Customer> customers) {
        return customers.stream()
                .collect(partitioningBy(Customer::isVIP));
    }

    public Map<Boolean, Long> partitionByVIPAndCount(List<Customer> customers) {
        return customers.stream()
                .collect(partitioningBy(Customer::isVIP, counting()));
    }

    public Map<Boolean, List<Customer>> partitionByAdult(List<Customer> customers) {
        return customers.stream()
                .collect(partitioningBy(customer -> customer.age() >= 18, toList()));
    }

    // --- toMap() as downstream collector ---

    // Group customers by city, then within each city produce a map of name → age
    public Map<String, Map<String, Integer>> groupByCityWithNameToAge(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city, toMap(Customer::name, Customer::age)));
    }

    // Group customers by city, then within each city produce a map of name → age
    public Map<String, List<Customer>> groupByNameAgeToCustomers(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(customer -> customer.name() + "_" + customer.age(), toList()));
    }

    // Same as above but wrapped in a CityReport domain object instead of a raw nested Map
    public Map<String, CityReport> groupByCityAsCityReport(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(
                        Customer::city,
                        collectingAndThen(
                                toMap(Customer::name, Customer::age),
                                CityReport::new
                        )
                ));
    }

    // Old behavior - raw nested Map, no domain object, harder to read and extend
    public Map<String, Map<String, Integer>> groupByCityAsCityReport2(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(
                        Customer::city,
                        toMap(Customer::name, Customer::age)
                ));
    }

    // --- main.mapping() downstream collector ---

    // Group customers by city, then map each group to just the customer names
    public Map<String, List<String>> groupByCityMappingToNames(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city, mapping(Customer::name, toList())));
    }

    // Group customers by city and join names into one comma-separated string per city
    public Map<String, String> groupByCityJoiningNames(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city, mapping(Customer::name, joining(", "))));
    }

    // Group by city and use collectingAndThen finisher to sort and freeze each city list
    public Map<String, List<String>> groupByCitySortedUnmodifiableNames(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(
                        Customer::city,
                        collectingAndThen(
                                mapping(Customer::name, toList()),
                                names -> names.stream().sorted().toList()
                        )
                ));
    }

    // Partition by VIP, then map each partition to a Set of names
    public Map<Boolean, Set<String>> partitionByVIPMappingToNames(List<Customer> customers) {
        return customers.stream()
                .collect(partitioningBy(Customer::isVIP, mapping(Customer::name, toSet())));
    }

    // Group by city, then map each group to a Set of purchased categories (flattened per group)
    public Map<String, Set<String>> groupByCityMappingToCategories(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city, flatMapping(customer -> customer.purchasedCategories().stream(), toSet())));
    }

    // --- filtering() downstream collector ---

    // Group customers by city, keeping only VIPs in each group
    public Map<String, List<Customer>> groupByCityFilteringVIPs(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city, filtering(Customer::isVIP, toList())));
    }

    // Partition by adult, then filter out non-VIPs within each partition
    public Map<Boolean, List<Customer>> partitionByAdultFilteringVIPs(List<Customer> customers) {
        return customers.stream()
                .collect(partitioningBy(c -> c.age() >= 18, filtering(Customer::isVIP, toList())));
    }

    // Group by city, filter adults only, and count them
    public Map<String, Long> groupByCityCountingAdults(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city, filtering(c -> c.age() >= 18, counting())));
    }


    public Map<String, CustomerCollections> groupByCityNamesAndAverageAge(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city, teeing(
                        flatMapping(c -> c.emails().stream(), toList()),
                        flatMapping(c -> c.purchasedCategories().stream(), toSet()),
                        CustomerCollections::new)));
    }

    // Return a list of maps, where each map represents aggregated data per city
    public List<Map<String, Object>> cityAggregates(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> cityData = new HashMap<>();
                    List<Customer> cityCustomers = entry.getValue();

                    cityData.put("city", entry.getKey());
                    cityData.put("names", cityCustomers.stream().map(Customer::name).toList());

                    return cityData;
                })
                .toList();
    }

    public List<Map<String, List<String>>> cityAggregates1(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, List<String>> cityData = new HashMap<>();
                    List<Customer> cityCustomers = entry.getValue();
                    
                    cityData.put("city", List.of(entry.getKey()));
                    cityData.put("names", cityCustomers.stream().map(Customer::name).toList());
                    
                    return cityData;
                })
                .toList();
    }

    public List<CityAggregate> cityAggregates2(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(Customer::city))
                .entrySet().stream()
                .map(entry -> new CityAggregate(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(Customer::name)
                                .toList()
                ))
                .toList();

    }

    public List<CityAggregate> cityAggregates3(List<Customer> customers) {
        return customers.stream()
                .collect(groupingBy(
                        Customer::city,
                        mapping(Customer::name, toList())
                ))
                .entrySet().stream()
                .map(entry -> new CityAggregate(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }
}
