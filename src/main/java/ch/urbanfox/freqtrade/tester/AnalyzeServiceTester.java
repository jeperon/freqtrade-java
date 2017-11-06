package ch.urbanfox.freqtrade.tester;

import java.time.ZonedDateTime;
import java.util.List;

import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import ch.urbanfox.freqtrade.FreqTradeApplication;
import ch.urbanfox.freqtrade.analyze.AnalyzeService;
import ch.urbanfox.freqtrade.exchange.FreqTradeExchangeService;
import ch.urbanfox.freqtrade.exchange.converter.BittrexDataConverter;
import eu.verdelhan.ta4j.TimeSeries;

public class AnalyzeServiceTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeServiceTester.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(FreqTradeApplication.class, args);

        FreqTradeExchangeService exchangeService = context.getBean(FreqTradeExchangeService.class);
        AnalyzeService analyzeService = context.getBean(AnalyzeService.class);

        ZonedDateTime minimumDate = ZonedDateTime.now().minusHours(6);
        List<BittrexChartData> rawTickers = exchangeService.fetchRawticker(new CurrencyPair("ETH/BTC"), minimumDate);
        TimeSeries tickers = new BittrexDataConverter().parseRawTickers(rawTickers);
        boolean buySignal = analyzeService.getBuySignal(tickers);
        
        LOGGER.info("buy signal: {}", buySignal);
        
        SpringApplication.exit(context);
    }

}
