package ch.urbanfox.freqtrade.analyze;

import eu.verdelhan.ta4j.TimeSeries;

public interface AnalyzeService {

    /**
     * Calculates a buy signal based several technical analysis indicators
     *
     * @param tickers the tickers to analyze
     * @return true if pair is good for buying, false otherwise
     */
    boolean getBuySignal(TimeSeries tickers);

}
