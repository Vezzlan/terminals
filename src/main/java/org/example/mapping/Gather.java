package org.example.mapping;

import org.example.model.Customer;

import java.util.List;
import java.util.stream.Gatherers;

public class Gather {

    // Split customers into fixed-size batches
    public List<List<Customer>> batchCustomers(List<Customer> customers, int batchSize) {
        return customers.stream()
                .gather(Gatherers.windowFixed(batchSize))
                .toList();
    }

    // Group mapped customer names into fixed-size batches using a built-in gatherer
    public List<List<String>> groupCustomerNamesFixedSize(List<Customer> customers, int groupSize) {
        return customers.stream()
                .map(Customer::name)
                .gather(Gatherers.windowFixed(groupSize))
                .toList();
    }

    // Produce overlapping sliding windows of customers
    public List<List<Customer>> slidingWindowCustomers(List<Customer> customers, int windowSize) {
        return customers.stream()
                .gather(Gatherers.windowSliding(windowSize))
                .toList();
    }

    // Running total of ages across the (ordered) customer list
    public List<Integer> runningAgeTotal(List<Customer> customers) {
        return customers.stream()
                .gather(Gatherers.scan(() -> 0, (runningSum, customer) -> runningSum + customer.age()))
                .toList();
    }

    // map() each customer to their age → gather sliding windows of 2 →
    // filter() only increasing pairs → collect to list
    public List<List<Integer>> increasingAgePairs(List<Customer> customers) {
        return customers.stream()
                .map(Customer::age)
                .gather(Gatherers.windowSliding(2))
                .filter(window -> window.get(1) > window.get(0))
                .toList();
    }

    // Find consecutive customer pairs from the same city
    // Uses: gather sliding windows of 2 → filter pairs with matching cities
    public List<List<Customer>> consecutiveCustomerPairsSameCity(List<Customer> customers) {
        return customers.stream()
                .gather(Gatherers.windowSliding(2))
                .filter(window -> window.get(0).city().equals(window.get(1).city()))
                .toList();
    }

    // Use scan() to track cumulative customer processing status
    // Returns a list showing how many customers have been processed at each step
    public List<String> cumulativeCustomerCountScan(List<Customer> customers) {
        return customers.stream()
                .gather(Gatherers.scan(
                        () -> 0,  // initialize counter at 0
                        (count, customer) -> count + 1  // increment for each customer
                ))
                .map(count -> "Processed " + count + " customer(s)")
                .toList();
    }
}
