package ch.urbanfox.freqtrade.analyze;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.urbanfox.freqtrade.analyze.indicator.BuySignalIndicator;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;

@Service
public class AnalyzeServiceImpl implements AnalyzeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeServiceImpl.class);

    @Override
    public boolean getBuySignal(TimeSeries tickers) {

        BuySignalIndicator buySignal = new BuySignalIndicator(tickers, 33, Decimal.valueOf(0.25));

        boolean signal = buySignal.getValue(tickers.getEndIndex());
        LOGGER.debug("buy_trigger: {} (end time={})", signal, tickers.getLastTick().getEndTime());
        return signal;
    }

}
