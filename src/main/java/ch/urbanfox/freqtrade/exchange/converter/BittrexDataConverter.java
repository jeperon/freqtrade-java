package ch.urbanfox.freqtrade.exchange.converter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;

import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

public class BittrexDataConverter {
    
    public TimeSeries parseRawTickers(List<BittrexChartData> rawData) {

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

}
