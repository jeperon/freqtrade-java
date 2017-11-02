package ch.urbanfox.freqtrade.analyze;

import java.io.IOException;

import org.knowm.xchange.currency.CurrencyPair;

public interface AnalyzeService {

    /**
     * Calculates a buy signal based several technical analysis indicators
     *
     * @param pair the trading pair
     * @return true if pair is good for buying, false otherwise
     *
     * @throws IOException if any communication error occurs while querying exchange for information
     */
    boolean getBuySignal(CurrencyPair pair) throws IOException;

}
