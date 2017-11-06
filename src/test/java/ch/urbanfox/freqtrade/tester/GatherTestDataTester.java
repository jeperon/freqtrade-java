package ch.urbanfox.freqtrade.tester;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.urbanfox.freqtrade.FreqTradeApplication;
import ch.urbanfox.freqtrade.exchange.FreqTradeExchangeService;

public class GatherTestDataTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatherTestDataTester.class);
    
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static void main(String[] args) throws IOException {

        ApplicationContext context = SpringApplication.run(FreqTradeApplication.class, args);

        FreqTradeExchangeService exchangeService = context.getBean(FreqTradeExchangeService.class);

        List<BittrexChartData> rawTickers = exchangeService.fetchRawticker(new CurrencyPair("ETH/BTC"));

        LOGGER.info("raw tickers: {}", rawTickers);

        ObjectMapper mapper = new ObjectMapper();

        String jsonAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(compact(rawTickers));

        Files.write(Paths.get("target", "ETH_BTC.json"), jsonAsString.getBytes(Charset.forName("UTF-8")));

        SpringApplication.exit(context);
    }

    private static List<Map<String, Object>> compact(List<BittrexChartData> rawTickers) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BittrexChartData bittrexChartData : rawTickers) {
            Map<String, Object> rawData = new HashMap<>();
            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            rawData.put("T", dateFormat.format(bittrexChartData.getTimeStamp()));
            rawData.put("O", bittrexChartData.getOpen());
            rawData.put("C", bittrexChartData.getClose());
            rawData.put("H", bittrexChartData.getHigh());
            rawData.put("L", bittrexChartData.getLow());
            rawData.put("V", bittrexChartData.getVolume());
            rawData.put("BV", bittrexChartData.getBaseVolume());
            result.add(rawData);
        }
        return result;
    }

}
