package org.example.mapping;

import org.example.model.Customer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class GatherTest {

    private final Gather gather = new Gather();

    private final List<Customer> customers = List.of(
            new Customer("Alice", 31, true, List.of("alice@example.com"), "Berlin", Set.of("Books", "Electronics")),
            new Customer("Bob", 17, false, List.of("bob@example.com"), "Berlin", Set.of("Toys")),
            new Customer("Carol", 26, true, List.of("carol@example.com"), "Paris", Set.of("Books", "Beauty")),
            new Customer("Dan", 40, false, List.of("dan@example.com"), "Paris", Set.of("Garden"))
    );

    @Test
    public void testBatchCustomersEvenSplit() {
        List<List<Customer>> result = gather.batchCustomers(customers, 2);
        assertEquals(2, result.size());
        assertEquals(List.of(customers.get(0), customers.get(1)), result.get(0));
        assertEquals(List.of(customers.get(2), customers.get(3)), result.get(1));
    }

    @Test
    public void testBatchCustomersWithRemainder() {
        List<List<Customer>> result = gather.batchCustomers(customers, 3);
        assertEquals(2, result.size());
        assertEquals(List.of(customers.get(0), customers.get(1), customers.get(2)), result.get(0));
        assertEquals(List.of(customers.get(3)), result.get(1));
    }

    @Test
    public void testGroupCustomerNamesFixedSize() {
        // map names first, then group them with built-in windowFixed(3)
        // names: [Alice, Bob, Carol, Dan] -> [[Alice, Bob, Carol], [Dan]]
        List<List<String>> result = gather.groupCustomerNamesFixedSize(customers, 3);
        assertEquals(2, result.size());
        assertEquals(List.of("Alice", "Bob", "Carol"), result.get(0));
        assertEquals(List.of("Dan"), result.get(1));
    }

    @Test
    public void testSlidingWindowCustomers() {
        List<List<Customer>> result = gather.slidingWindowCustomers(customers, 2);
        assertEquals(3, result.size());
        assertEquals(List.of(customers.get(0), customers.get(1)), result.get(0));
        assertEquals(List.of(customers.get(1), customers.get(2)), result.get(1));
        assertEquals(List.of(customers.get(2), customers.get(3)), result.get(2));
    }

    @Test
    public void testRunningAgeTotal() {
        // Alice=31, Bob=17, Carol=26, Dan=40 → running sums: 31, 48, 74, 114
        List<Integer> result = gather.runningAgeTotal(customers);
        assertEquals(4, result.size());
        assertEquals((Integer) 31, result.get(0));
        assertEquals((Integer) 48, result.get(1));
        assertEquals((Integer) 74, result.get(2));
        assertEquals((Integer) 114, result.get(3));
    }

    @Test
    public void testIncreasingAgePairs() {
        // This test demonstrates the full stream pipeline: map → gather → filter → terminal

        // Step 1: Extract ages from customers
        // Input customers: Alice(31), Bob(17), Carol(26), Dan(40)
        // After map(Customer::age): [31, 17, 26, 40]

        // Step 2: Apply gathering operation - sliding window of size 2
        // Gatherer windowSliding(2) creates overlapping consecutive pairs:
        // [31, 17] - first pair (indices 0-1)
        // [17, 26] - second pair (indices 1-2)
        // [26, 40] - third pair (indices 2-3)

        // Step 3: Filter - keep only pairs where second element > first element
        // [31, 17] → FILTERED OUT (17 < 31, not increasing)
        // [17, 26] → KEPT (26 > 17, increasing) ✓
        // [26, 40] → KEPT (40 > 26, increasing) ✓

        // Step 4: Terminal operation - collect to list

        List<List<Integer>> result = gather.increasingAgePairs(customers);

        // Verify we get exactly 2 increasing pairs
        assertEquals(2, result.size());

        // Verify the first increasing pair is [17, 26]
        // (Bob's age 17 followed by Carol's age 26)
        assertEquals(List.of(17, 26), result.get(0));

        // Verify the second increasing pair is [26, 40]
        // (Carol's age 26 followed by Dan's age 40)
        assertEquals(List.of(26, 40), result.get(1));
    }

    @Test
    public void testConsecutiveCustomerPairsSameCity() {
        // This test finds consecutive customers who live in the same city
        // Demonstrates: gather sliding windows → filter by city match

        // Customer list and their cities:
        // Alice → Berlin
        // Bob   → Berlin
        // Carol → Paris
        // Dan   → Paris

        // Sliding window pairs:
        // [Alice(Berlin), Bob(Berlin)]   → same city ✓ KEPT
        // [Bob(Berlin), Carol(Paris)]    → different cities ✗ FILTERED OUT
        // [Carol(Paris), Dan(Paris)]     → same city ✓ KEPT

        List<List<Customer>> result = gather.consecutiveCustomerPairsSameCity(customers);

        // Verify we found exactly 2 consecutive same-city pairs
        assertEquals(2, result.size());

        // First pair: both in Berlin
        assertEquals(List.of(customers.get(0), customers.get(1)), result.get(0));

        // Second pair: both in Paris
        assertEquals(List.of(customers.get(2), customers.get(3)), result.get(1));
    }

    @Test
    public void testCumulativeCustomerCountScan() {
        // This test uses the scan() built-in gatherer
        // scan() is like reduce but emits each intermediate result instead of just the final one

        // With 4 customers, scan with counter increment produces:
        // Process Alice:  count becomes 1 → "Processed 1 customer(s)"
        // Process Bob:    count becomes 2 → "Processed 2 customer(s)"
        // Process Carol:  count becomes 3 → "Processed 3 customer(s)"
        // Process Dan:    count becomes 4 → "Processed 4 customer(s)"

        List<String> result = gather.cumulativeCustomerCountScan(customers);

        // Verify we have a status message for each customer
        assertEquals(4, result.size());
        assertEquals("Processed 1 customer(s)", result.get(0));
        assertEquals("Processed 2 customer(s)", result.get(1));
        assertEquals("Processed 3 customer(s)", result.get(2));
        assertEquals("Processed 4 customer(s)", result.get(3));
    }
}