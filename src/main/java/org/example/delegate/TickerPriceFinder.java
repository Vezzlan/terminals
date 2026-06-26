package org.example.delegate;

import java.math.BigDecimal;
import java.util.Map;

public class TickerPriceFinder {

    private static final Map<String, BigDecimal> PRICE_BY_TICKER = Map.of(
            "ABC", new BigDecimal("6.01"),
            "XYZ", new BigDecimal("10.50")
    );

    public BigDecimal priceFor(final String ticker) {
        final BigDecimal price = PRICE_BY_TICKER.get(ticker);
        if (price == null) {
            throw new IllegalArgumentException("Unknown ticker: " + ticker);
        }
        return price;
    }
}
