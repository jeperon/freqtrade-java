package ch.urbanfox.freqtrade.tester;

import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import ch.urbanfox.freqtrade.FreqTradeApplication;
import ch.urbanfox.freqtrade.analyze.AnalyzeService;

public class AnalyzeServiceTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeServiceTester.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(FreqTradeApplication.class, args);

        AnalyzeService analyzeService = context.getBean(AnalyzeService.class);

        boolean buySignal = analyzeService.getBuySignal(new CurrencyPair("ETH/BTC"));
        
        LOGGER.info("buy signal: {}", buySignal);
        
        SpringApplication.exit(context);
    }

}
