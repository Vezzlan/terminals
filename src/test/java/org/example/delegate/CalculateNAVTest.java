package org.example.delegate;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculateNAVTest {

    @Test
    void calculateNAV() {
        final CalculateNAV calculateNAV = new CalculateNAV(_ -> new BigDecimal("6.01"));

        BigDecimal expected = new BigDecimal("6010.00");
        BigDecimal actual = calculateNAV.calculateNAV("ABC", 1000);
        BigDecimal delta = expected.subtract(actual);
        assertEquals(0, delta.doubleValue(), 0.0001, "NAV calculation is incorrect");
    }

    @Test
    void calculateNAVUsesTickerFromFinder() {
        final TickerPriceFinder tickerPriceFinder = new TickerPriceFinder();
        final CalculateNAV calculateNAV = new CalculateNAV(tickerPriceFinder::priceFor);

        assertEquals(new BigDecimal("601.00"), calculateNAV.calculateNAV("ABC", 100));
        assertEquals(new BigDecimal("1050.00"), calculateNAV.calculateNAV("XYZ", 100));
    }
}