package ch.urbanfox.freqtrade.tester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.urbanfox.freqtrade.analyze.AnalyzeService;
import ch.urbanfox.freqtrade.analyze.AnalyzeServiceImpl;
import ch.urbanfox.freqtrade.exchange.converter.BittrexDataConverter;
import eu.verdelhan.ta4j.TimeSeries;

public class StrategyTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyTester.class);

    private static final int WINDOW_SIZE = 72;

    public static void main(String[] args) throws IOException {

        byte[] jsonAsBytes = Files.readAllBytes(Paths.get("src", "test", "resources", "data", "ETH_BTC.json"));

        ObjectMapper mapper = new ObjectMapper();

        List<BittrexChartData> result = mapper.readValue(jsonAsBytes, new TypeReference<List<BittrexChartData>>() {});

        LOGGER.info("Parsed json file: {} ticks", result.size());

        AnalyzeService analyzeService = new AnalyzeServiceImpl();
        BittrexDataConverter converter = new BittrexDataConverter();

        int buySignalCount = 0;
        
        for (int i = 0; i < (result.size() - WINDOW_SIZE); i++) {
            List<BittrexChartData> sample = result.stream()
                    .skip(i)
                    .limit(WINDOW_SIZE)
                    .collect(Collectors.toList());

            TimeSeries timeSeries = converter.parseRawTickers(sample);

            boolean buySignal = analyzeService.getBuySignal(timeSeries);

            LOGGER.info("Buy signal: {}, timestamp: {}", buySignal, timeSeries.getLastTick().getBeginTime());

            if (buySignal) {
                buySignalCount++;
            }
        }
        
        LOGGER.info("Found {} buy signals", buySignalCount);
    }

}
