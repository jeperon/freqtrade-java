package ch.urbanfox.freqtrade.analyze;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;
import org.knowm.xchange.bittrex.service.BittrexChartDataPeriodType;
import org.knowm.xchange.bittrex.service.BittrexMarketDataServiceRaw;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.urbanfox.freqtrade.analyze.indicator.BuySignalIndicator;
import ch.urbanfox.freqtrade.analyze.indicator.SwapIndicator;
import ch.urbanfox.freqtrade.analyze.indicator.UpSwingIndicator;
import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.AverageDirectionalMovementIndicator;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.ParabolicSarIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;

@Component
public class AnalyzeServiceImpl implements AnalyzeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeServiceImpl.class);

    private final BittrexMarketDataServiceRaw marketDataService;

    @Autowired
    public AnalyzeServiceImpl(Exchange exchange) {
        this.marketDataService = (BittrexMarketDataServiceRaw) exchange.getMarketDataService();
    }

    /**
     * Request ticker data from Bittrex for a given currency pair
     */
    List<BittrexChartData> fetchRawticker(CurrencyPair pair, ZonedDateTime minimumDate) throws IOException {
        LOGGER.debug("Fetching ticks for: {}, minimum date: {}", pair, minimumDate);

        List<BittrexChartData> ticks = marketDataService.getBittrexChartData(pair, BittrexChartDataPeriodType.FIVE_MIN);

        LOGGER.debug("Found {} ticks", ticks.size());

        List<BittrexChartData> filteredTicks = ticks.stream()
                .filter(t -> t.getTimeStamp().after(Date.from(minimumDate.toInstant())))
                .collect(Collectors.toList());

        LOGGER.debug("After filtering: {} ticks", filteredTicks.size());

        return filteredTicks;
    }

    TimeSeries parseRawTickers(List<BittrexChartData> rawData) {

        List<Tick> ticks = rawData.stream()
                .map(this::convertTick)
                .collect(Collectors.toList());

        return new BaseTimeSeries(ticks);
    }

    private BaseTick convertTick(BittrexChartData data) {

        return new BaseTick(ZonedDateTime.ofInstant(data.getTimeStamp().toInstant(), ZoneId.systemDefault()),
                Decimal.valueOf(data.getOpen().toString()),
                Decimal.valueOf(data.getHigh().toString()),
                Decimal.valueOf(data.getLow().toString()),
                Decimal.valueOf(data.getClose().toString()),
                Decimal.valueOf(data.getVolume().toString()));
    }

    public BuySignalIndicator computeBuySignal(TimeSeries dataframe) {

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(dataframe);

        EMAIndicator emaIndicator = new EMAIndicator(closePriceIndicator, 33);
        ParabolicSarIndicator sarIndicator = new ParabolicSarIndicator(dataframe, 33);
        AverageDirectionalMovementIndicator adxIndicator = new AverageDirectionalMovementIndicator(dataframe, 33);

        // wait for stable turn from bearish to bullish market
        SwapIndicator swapIndicator = new SwapIndicator(closePriceIndicator, sarIndicator);

        // consider prices above ema to be in upswing
        UpSwingIndicator upSwingIndicator = new UpSwingIndicator(closePriceIndicator, emaIndicator);

        return new BuySignalIndicator(upSwingIndicator, swapIndicator, adxIndicator);
    }

    @Override
    public boolean getBuySignal(CurrencyPair pair) throws IOException {
        ZonedDateTime minimumDate = ZonedDateTime.now().minusHours(6);
        List<BittrexChartData> rawTickers = fetchRawticker(pair, minimumDate);
        TimeSeries tickers = parseRawTickers(rawTickers);

        BuySignalIndicator buySignal = computeBuySignal(tickers);

        Tick latest = tickers.getLastTick();

        // Check if dataframe is out of date
        ZonedDateTime signalDate = latest.getEndTime();
        if (signalDate.isBefore(ZonedDateTime.now().minusMinutes(10))) {
            LOGGER.debug("Signal is outdated ({}) for pair: {}", signalDate, pair);
            return false;
        }

        boolean signal = buySignal.getValue(tickers.getEndIndex());
        LOGGER.debug("buy_trigger: {} (pair={}, signal={})", signalDate, pair, signal);
        return signal;
    }

}
